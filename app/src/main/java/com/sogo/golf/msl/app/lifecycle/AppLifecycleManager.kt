package com.sogo.golf.msl.app.lifecycle

import com.sogo.golf.msl.domain.repository.remote.AuthRepository
import com.sogo.golf.msl.domain.usecase.date.ResetStaleDataUseCase
import com.sogo.golf.msl.domain.usecase.date.ValidateGameDataFreshnessUseCase
import javax.inject.Inject
import javax.inject.Singleton

enum class AppResumeAction {
    Continue,           // Data is fresh, continue where user left off
    NavigateToHome      // Data is stale, navigate to home screen
}

@Singleton
class AppLifecycleManager @Inject constructor(
    private val validateGameDataFreshnessUseCase: ValidateGameDataFreshnessUseCase,
    private val resetStaleDataUseCase: ResetStaleDataUseCase,
    private val authRepository: AuthRepository
) {

    suspend fun onAppResumed(): AppResumeAction {
        android.util.Log.d("AppLifecycleManager", "=== APP RESUMED ===")

        // Only check date if user is logged in
        if (!authRepository.isUserLoggedIn()) {
            android.util.Log.d("AppLifecycleManager", "User not logged in, no date check needed")
            return AppResumeAction.Continue
        }

        android.util.Log.d("AppLifecycleManager", "User is logged in, checking data freshness...")

        val isDataFresh = validateGameDataFreshnessUseCase()

        return if (isDataFresh) {
            android.util.Log.d("AppLifecycleManager", "✅ Data is fresh for today - continuing where user left off")
            AppResumeAction.Continue
        } else {
            android.util.Log.d("AppLifecycleManager", "📅 Data is stale - resetting and navigating to home")

            // Reset stale data and fetch fresh data
            when (val resetResult = resetStaleDataUseCase()) {
                is Result -> {
                    if (resetResult.isSuccess) {
                        android.util.Log.d("AppLifecycleManager", "✅ Stale data reset completed successfully")
                    } else {
                        android.util.Log.w("AppLifecycleManager", "⚠️ Stale data reset failed: ${resetResult.exceptionOrNull()}")
                        // Continue anyway - better to have some data than none
                    }
                }
            }

            AppResumeAction.NavigateToHome
        }
    }

    /**
     * Call this when app is first launched (not resumed from background)
     * No date checking needed on launch - user will go through normal flow
     */
    fun onAppLaunched() {
        android.util.Log.d("AppLifecycleManager", "=== APP LAUNCHED ===")
        android.util.Log.d("AppLifecycleManager", "No date validation needed on fresh launch")
    }
}