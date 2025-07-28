// app/src/main/java/com/sogo/golf/msl/app/update/AppUpdateManager.kt
package com.sogo.golf.msl.app.update

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class UpdateState(
    val isCheckingForUpdate: Boolean = false,
    val updateAvailable: Boolean = false,
    val updateError: String? = null,
    val updateInfo: AppUpdateInfo? = null
)

@Singleton
class AppUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AppUpdateManager"
    }

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)

    private val _updateState = MutableStateFlow(UpdateState())
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    // Note: No install state listener needed since we only use immediate updates

    /**
     * Check for app updates and determine update strategy
     */
    fun checkForUpdates(
        activity: Activity,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        if (activity !is LifecycleOwner) {
            Log.e(TAG, "Activity must implement LifecycleOwner")
            return
        }

        val lifecycleOwner = activity as LifecycleOwner

        lifecycleOwner.lifecycleScope.launch {
            try {
                _updateState.value = _updateState.value.copy(
                    isCheckingForUpdate = true,
                    updateError = null
                )

                Log.d(TAG, "Checking for app updates...")

                val appUpdateInfoTask = appUpdateManager.appUpdateInfo
                appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                    Log.d(TAG, "Update check completed. Availability: ${appUpdateInfo.updateAvailability()}")
                    Log.d(TAG, "Available version code: ${appUpdateInfo.availableVersionCode()}")
                    Log.d(TAG, "Client version staleness: ${appUpdateInfo.clientVersionStalenessDays()}")

                    _updateState.value = _updateState.value.copy(
                        isCheckingForUpdate = false,
                        updateInfo = appUpdateInfo
                    )

                    when (appUpdateInfo.updateAvailability()) {
                        UpdateAvailability.UPDATE_AVAILABLE -> {
                            Log.d(TAG, "✅ Update available!")
                            _updateState.value = _updateState.value.copy(updateAvailable = true)

                            // ALWAYS force immediate update - no exceptions
                            val staleness = appUpdateInfo.clientVersionStalenessDays() ?: 0
                            Log.d(TAG, "🚨 Update available (app is $staleness days old) - FORCING IMMEDIATE update")
                            startImmediateUpdate(activity, appUpdateInfo, activityResultLauncher)
                        }
                        UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                            Log.d(TAG, "✅ App is up to date")
                            _updateState.value = _updateState.value.copy(updateAvailable = false)
                        }
                        UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                            Log.d(TAG, "🔄 Developer triggered update in progress")
                            // Resume any in-progress update
                            resumeUpdateIfNeeded(activity, appUpdateInfo, activityResultLauncher)
                        }
                        else -> {
                            Log.d(TAG, "❓ Unknown update availability: ${appUpdateInfo.updateAvailability()}")
                        }
                    }
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "❌ Failed to check for updates", exception)

                    val errorMessage = when {
                        exception.message?.contains("ERROR_APP_NOT_OWNED") == true -> {
                            "Development build - update check skipped"
                        }
                        else -> "Failed to check for updates: ${exception.message}"
                    }

                    _updateState.value = _updateState.value.copy(
                        isCheckingForUpdate = false,
                        updateError = errorMessage
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception during update check", e)
                _updateState.value = _updateState.value.copy(
                    isCheckingForUpdate = false,
                    updateError = "Update check failed: ${e.message}"
                )
            }
        }
    }

    /**
     * Start immediate update (blocks the app until update is complete)
     * This is now the ONLY update strategy - no flexible updates
     */
    private fun startImmediateUpdate(
        activity: Activity,
        appUpdateInfo: AppUpdateInfo,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        try {
            Log.d(TAG, "🚨 FORCING immediate update - user must install to continue...")

            if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                val updateOptions = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()

                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activityResultLauncher,
                    updateOptions
                )
            } else {
                Log.e(TAG, "❌ Immediate update not allowed by Google Play")
                _updateState.value = _updateState.value.copy(
                    updateError = "Update required but not supported on this device. Please update manually from Google Play."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start immediate update", e)
            _updateState.value = _updateState.value.copy(
                updateError = "Failed to start required update: ${e.message}"
            )
        }
    }

    /**
     * Handle the result of an update flow
     */
    fun handleUpdateResult(result: ActivityResult) {
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                Log.d(TAG, "✅ Update completed successfully")
                // For immediate updates, this usually means the app will restart
            }
            Activity.RESULT_CANCELED -> {
                Log.w(TAG, "⚠️ User canceled the update")
                _updateState.value = _updateState.value.copy(
                    updateError = "Update was canceled. Please update to continue using the app."
                )
            }
            else -> {
                Log.e(TAG, "❌ Update failed with result code: ${result.resultCode}")
                _updateState.value = _updateState.value.copy(
                    updateError = "Update failed. Please try again or update manually from Google Play."
                )
            }
        }
    }

    /**
     * Resume any in-progress updates - always immediate updates
     */
    private fun resumeUpdateIfNeeded(
        activity: Activity,
        appUpdateInfo: AppUpdateInfo,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
            Log.d(TAG, "🔄 Resuming immediate update...")
            startImmediateUpdate(activity, appUpdateInfo, activityResultLauncher)
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _updateState.value = _updateState.value.copy(updateError = null)
    }

    /**
     * Clean up resources - simplified since no listeners needed for immediate updates
     */
    fun cleanup() {
        // No cleanup needed for immediate updates
        Log.d(TAG, "AppUpdateManager cleanup completed")
    }
}