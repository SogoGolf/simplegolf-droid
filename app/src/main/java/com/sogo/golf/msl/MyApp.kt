package com.sogo.golf.msl

import android.app.Application
import android.util.Log
import com.jakewharton.threetenabp.AndroidThreeTen
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)

        // ✅ FORCE FONT SCALE TO 1.0 across the entire app
        enforceNormalFontScale()

//        SentryAndroid.init(this) { options ->
//            options.dsn = BuildConfig.SENTRY_DSN
//            options.isEnableAppComponentBreadcrumbs = true
//            options.isEnableActivityLifecycleBreadcrumbs = true
//            options.isEnableAppLifecycleBreadcrumbs = true
//            options.isEnableSystemEventBreadcrumbs = true
//            options.isEnableNetworkEventBreadcrumbs = true
//            options.isEnableUserInteractionBreadcrumbs = true
//            options.isAttachScreenshot = true
//            options.isAttachViewHierarchy = true
//            options.isEnableAppStartProfiling = true
//            options.tracesSampleRate = 1.0
//
//            // Unified beforeSend filter
//            options.beforeSend = SentryOptions.BeforeSendCallback { event, _ ->
//                // 1) drop DEBUG‑level events
//                if (event.level == SentryLevel.DEBUG) {
//                    return@BeforeSendCallback null
//                }
//
//                // 2) drop coroutine cancellations
//                //https://sogo-yc.sentry.io/issues/undefined/events/2953518d536146d8aff911feed9532a4/
//                val thr: Throwable? = event.throwable
//                if (thr != null) {
//                    var t: Throwable? = thr
//                    while (t != null) {
//                        if (t is CancellationException) {
//                            return@BeforeSendCallback null
//                        }
//                        t = t.cause
//                    }
//                }
//
//                // 3) Filter out Google Play Billing ProxyBillingActivity crashes (not our code)
//                event.exceptions?.forEach { exception ->
//                    exception.stacktrace?.frames?.forEach { frame ->
//                        if (frame.module?.contains("com.android.billingclient.api.ProxyBillingActivity") == true) {
//                            return@BeforeSendCallback null
//                        }
//                    }
//                }
//
//                // otherwise keep the event
//                event
//            }
//        }
//
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(PurchasesConfiguration.Builder(this, BuildConfig.REVENUECAT).build())
        Log.d("MyApp", "Revenuecat initialised...")
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
