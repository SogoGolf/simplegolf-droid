package com.sogo.golf.msl.data.messaging

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManagerWrapper @Inject constructor(
    private val context: Context
) {
    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun notify(id: Int, notification: Notification) {
        notificationManager.notify(id, notification)
    }

    fun cancel(id: Int) {
        notificationManager.cancel(id)
    }

    fun cancelAll() {
        notificationManager.cancelAll()
    }
}