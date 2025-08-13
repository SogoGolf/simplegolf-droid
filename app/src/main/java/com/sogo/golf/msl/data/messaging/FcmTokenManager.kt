package com.sogo.golf.msl.data.messaging

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenManager @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging
) {
    companion object {
        private const val TAG = "FcmTokenManager"
    }

    suspend fun getCurrentToken(): String? {
        return try {
            val token = firebaseMessaging.token.await()
            Log.d(TAG, "FCM Token retrieved: $token")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FCM token", e)
            null
        }
    }

    fun subscribeToTopic(topic: String) {
        firebaseMessaging.subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "Subscribed to topic: $topic"
                if (!task.isSuccessful) {
                    msg = "Failed to subscribe to topic: $topic"
                }
                Log.d(TAG, msg)
            }
    }

    fun unsubscribeFromTopic(topic: String) {
        firebaseMessaging.unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "Unsubscribed from topic: $topic"
                if (!task.isSuccessful) {
                    msg = "Failed to unsubscribe from topic: $topic"
                }
                Log.d(TAG, msg)
            }
    }

    fun deleteToken() {
        firebaseMessaging.deleteToken()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "FCM token deleted successfully")
                } else {
                    Log.e(TAG, "Failed to delete FCM token", task.exception)
                }
            }
    }
}