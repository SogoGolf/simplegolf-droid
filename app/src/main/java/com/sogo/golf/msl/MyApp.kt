package com.sogo.golf.msl

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)

        // ✅ FORCE FONT SCALE TO 1.0 across the entire app
        enforceNormalFontScale()
    }

    private fun enforceNormalFontScale() {
        val configuration = resources.configuration
        if (configuration.fontScale != 1.0f) {
            configuration.fontScale = 1.0f
            val metrics = resources.displayMetrics
            metrics.scaledDensity = metrics.density * configuration.fontScale

            // This method works on all API levels 24+ (even though deprecated in API 25)
            // It's still the most reliable way to override font scaling globally
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, metrics)
        }
    }
}
