package com.sogo.golf.msl.app.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.sogo.golf.msl.BuildConfig
import com.sogo.golf.msl.data.network.api.SogoMongoApiService
import com.sogo.golf.msl.domain.model.MobileAppVersionPlatformConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class UpdateState(
    val isCheckingForUpdate: Boolean = false,
    val isUpdateRequired: Boolean = false,
    val isOptionalUpdateAvailable: Boolean = false,
    val updateMessage: String = "",
    val minimumRequiredVersion: String = ""
)

@Singleton
class AppUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sogoMongoApiService: SogoMongoApiService
) {
    companion object {
        private const val TAG = "AppUpdateManager"
        private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.sogo.golf.msl"
        private const val PREFS_NAME = "app_update_prefs"
        private const val KEY_DISMISSED_VERSION_BUCKET = "dismissed_optional_update_version_bucket"
    }

    private val _updateState = MutableStateFlow(UpdateState())
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private var dismissedOptionalVersionBucket: String?
        get() = prefs.getString(KEY_DISMISSED_VERSION_BUCKET, null)
        set(value) {
            if (value.isNullOrEmpty()) {
                prefs.edit().remove(KEY_DISMISSED_VERSION_BUCKET).apply()
            } else {
                prefs.edit().putString(KEY_DISMISSED_VERSION_BUCKET, value).apply()
            }
        }

    suspend fun checkForUpdate(platform: String = "android") {
        _updateState.value = _updateState.value.copy(isCheckingForUpdate = true)

        val currentVersion = BuildConfig.VERSION_NAME
        if (currentVersion.isBlank()) {
            Log.e(TAG, "❌ Failed to get current app version")
            _updateState.value = _updateState.value.copy(isCheckingForUpdate = false)
            return
        }

        try {
            val authHeader = "Bearer ${BuildConfig.MONGO_API_KEY}"
            val response = sogoMongoApiService.getMobileAppVersionPlatformConfig(platform, authHeader)

            if (response.isSuccessful) {
                val config = response.body() ?: MobileAppVersionPlatformConfig()
                handleConfig(currentVersion, config)
            } else {
                Log.e(TAG, "❌ Failed to check for app update: ${response.code()} - ${response.message()}")
                clearUpdateState()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to check for app update: ${e.message}")
            clearUpdateState()
        }

        _updateState.value = _updateState.value.copy(isCheckingForUpdate = false)
    }

    private fun handleConfig(currentVersion: String, config: MobileAppVersionPlatformConfig) {
        val needsUpdate = isVersionOutdated(currentVersion, config.minimumRequiredVersion)

        if (!needsUpdate) {
            clearUpdateState()
            return
        }

        val resolvedMessage = config.updateMessage.ifEmpty {
            "A newer version of the app is available. Please update to continue."
        }

        if (config.forceUpdateEnabled) {
            // Blocking prompt — no "Later" button
            _updateState.value = _updateState.value.copy(
                isUpdateRequired = true,
                isOptionalUpdateAvailable = false,
                updateMessage = resolvedMessage,
                minimumRequiredVersion = config.minimumRequiredVersion
            )
        } else {
            // Dismissible prompt — user can tap "Later"
            val bucket = promptVersionBucket(config.minimumRequiredVersion)
            if (dismissedOptionalVersionBucket == bucket) {
                clearUpdateState()
                return
            }

            _updateState.value = _updateState.value.copy(
                isUpdateRequired = false,
                isOptionalUpdateAvailable = true,
                updateMessage = resolvedMessage,
                minimumRequiredVersion = config.minimumRequiredVersion
            )
        }
    }

    fun openPlayStore() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to open Play Store: ${e.message}")
        }
    }

    fun dismissOptionalUpdate() {
        val version = _updateState.value.minimumRequiredVersion
        clearUpdateState()
        dismissedOptionalVersionBucket = if (version.isBlank()) null else promptVersionBucket(version)
    }

    private fun clearUpdateState() {
        _updateState.value = _updateState.value.copy(
            isUpdateRequired = false,
            isOptionalUpdateAvailable = false,
            updateMessage = "",
            minimumRequiredVersion = ""
        )
    }

    private fun isVersionOutdated(currentVersion: String, minimumVersion: String): Boolean {
        val currentComponents = parseVersion(currentVersion)
        val minimumComponents = parseVersion(minimumVersion)
        val maxCount = maxOf(currentComponents.size, minimumComponents.size)

        for (index in 0 until maxCount) {
            val current = currentComponents.getOrElse(index) { 0 }
            val minimum = minimumComponents.getOrElse(index) { 0 }

            if (current < minimum) return true
            if (current > minimum) return false
        }

        return false
    }

    private fun parseVersion(version: String): List<Int> {
        return Regex("\\d+").findAll(version).mapNotNull { it.value.toIntOrNull() }.toList()
    }

    private fun promptVersionBucket(version: String): String {
        val components = parseVersion(version)
        val major = components.getOrElse(0) { 0 }
        val minor = components.getOrElse(1) { 0 }
        return "$major.$minor"
    }
}
