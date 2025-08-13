package com.sogo.golf.msl.data.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sogo.golf.msl.MainActivity
import com.sogo.golf.msl.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SimpleGolfMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmTokenManager: FcmTokenManager

    companion object {
        private const val TAG = "SimpleGolfFCMService"
        private const val CHANNEL_ID = "simplegolf_notifications"
        private const val CHANNEL_NAME = "Simple Golf Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for Simple Golf app"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "From: ${remoteMessage.from}")
        
        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataPayload(remoteMessage.data)
        }
        
        // Check if message contains notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Message Notification Body: ${notification.body}")
            showNotification(
                title = notification.title ?: "Simple Golf",
                body = notification.body ?: "",
                data = remoteMessage.data
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        
        // Send token to your server
        sendTokenToServer(token)
    }

    private fun handleDataPayload(data: Map<String, String>) {
        // Handle different types of data messages
        when (data["type"]) {
            "round_reminder" -> {
                showNotification(
                    title = data["title"] ?: "Round Reminder",
                    body = data["body"] ?: "You have a scheduled round today",
                    data = data
                )
            }
            "competition_update" -> {
                showNotification(
                    title = data["title"] ?: "Competition Update",
                    body = data["body"] ?: "Check out the latest competition results",
                    data = data
                )
            }
            "general" -> {
                showNotification(
                    title = data["title"] ?: "Simple Golf",
                    body = data["body"] ?: "",
                    data = data
                )
            }
            else -> {
                // Default notification
                showNotification(
                    title = data["title"] ?: "Simple Golf",
                    body = data["body"] ?: "You have a new notification",
                    data = data
                )
            }
        }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String> = emptyMap()) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            
            // Add any extra data from the notification
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // You may want to create a specific notification icon
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        val systemNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        systemNotificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        
        // Show toast notification
        showToast("📱 $title: $body")
    }
    
    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendTokenToServer(token: String) {
        // TODO: Implement token registration with your backend
        // This should send the FCM token to your server so it can send notifications
        Log.d(TAG, "Sending token to server: $token")
        
        // Example implementation would be:
        // 1. Get current user ID
        // 2. Make API call to register token
        // 3. Store token locally for reference
    }
}