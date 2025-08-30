package com.sogo.golf.msl

import android.app.Application
import android.util.Log
import com.jakewharton.threetenabp.AndroidThreeTen
import com.google.firebase.FirebaseApp
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.google.firebase.messaging.FirebaseMessaging
import com.onesignal.OneSignal
import dagger.hilt.android.HiltAndroidApp
import io.sentry.SentryLevel
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException


@HiltAndroidApp
class MyApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase first
        try {
            FirebaseApp.initializeApp(this)
            Log.d("MyApp", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("MyApp", "Firebase initialization failed", e)
        }
        
        AndroidThreeTen.init(this)

        // ✅ FORCE FONT SCALE TO 1.0 across the entire app
        enforceNormalFontScale()

        SentryAndroid.init(this) { options ->
            options.dsn = BuildConfig.SENTRY_DSN
            options.isEnableAppComponentBreadcrumbs = true
            options.isEnableActivityLifecycleBreadcrumbs = true
            options.isEnableAppLifecycleBreadcrumbs = true
            options.isEnableSystemEventBreadcrumbs = true
            options.isEnableNetworkEventBreadcrumbs = true
            options.isEnableUserInteractionBreadcrumbs = true
            options.isAttachScreenshot = true
            options.isAttachViewHierarchy = true
            options.isEnableAppStartProfiling = true
            options.tracesSampleRate = 1.0
            options.logs.isEnabled = true

            // Unified beforeSend filter
            options.beforeSend = SentryOptions.BeforeSendCallback { event, _ ->
                // 1) drop DEBUG‑level events
                if (event.level == SentryLevel.DEBUG) {
                    return@BeforeSendCallback null
                }

                // 2) drop coroutine cancellations
                //https://sogo-yc.sentry.io/issues/undefined/events/2953518d536146d8aff911feed9532a4/
                val thr: Throwable? = event.throwable
                if (thr != null) {
                    var t: Throwable? = thr
                    while (t != null) {
                        if (t is CancellationException) {
                            return@BeforeSendCallback null
                        }
                        t = t.cause
                    }
                }

                // 3) Filter out Google Play Billing ProxyBillingActivity crashes (not our code)
                event.exceptions?.forEach { exception ->
                    exception.stacktrace?.frames?.forEach { frame ->
                        if (frame.module?.contains("com.android.billingclient.api.ProxyBillingActivity") == true) {
                            return@BeforeSendCallback null
                        }
                    }
                }

                // otherwise keep the event
                event
            }
        }

        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(PurchasesConfiguration.Builder(this, BuildConfig.REVENUECAT).build())
        Log.d("MyApp", "Revenuecat initialised...")

        //OneSignal SDK init
        // Enable verbose logging for debugging (remove in production)
        OneSignal.Debug.logLevel = com.onesignal.debug.LogLevel.VERBOSE
        // Initialize with your OneSignal App ID
        OneSignal.initWithContext(this, "f65531f8-5975-47ff-8562-2b9a5bea9f1c")


        // Initialize FCM token after a delay to ensure Firebase is ready
        CoroutineScope(Dispatchers.Main).launch {
            kotlinx.coroutines.delay(1000) // Wait 1 second for Firebase to initialize
            initializeFcmToken()
        }
    }

    private suspend fun initializeFcmToken() {
        try {
            Log.d("MyApp", "Starting FCM initialization...")
            
            // Check if Firebase app is initialized
            val firebaseApps = FirebaseApp.getApps(this)
            Log.d("MyApp", "Firebase apps count: ${firebaseApps.size}")
            firebaseApps.forEach { app ->
                Log.d("MyApp", "Firebase app: ${app.name}, options: ${app.options.applicationId}")
            }
            
            // Get FCM token directly from FirebaseMessaging
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d("MyApp", "FCM Token: $token")
            
            // Subscribe to MSL notifications topic
            FirebaseMessaging.getInstance().subscribeToTopic("msl_notification")
                .addOnCompleteListener { task ->
                    val msg = if (task.isSuccessful) {
                        "Subscribed to msl_notification"
                    } else {
                        "Failed to subscribe to msl_notification"
                    }
                    Log.d("MyApp", msg)
                }
            
            // TODO: Send token to your backend server for user-specific notifications
            // This would typically involve making an API call to register the token
            // with the user's account
        } catch (e: Exception) {
            Log.e("MyApp", "Failed to initialize FCM token", e)
        }
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
