package com.example.unitask.notifications

import android.app.NotificationManager
import android.content.Context
import android.app.PendingIntent

class NotificationHelper(private val context: Context, private val notificationManager: NotificationManager) {
    fun createChannels() {
        // TODO: create NotificationChannel(s) for reminders and rewards
    }

    fun showReminderNotification(id: String, title: String, body: String, pendingIntent: PendingIntent?) {
        // TODO: build and show notification
    }

    fun showRewardNotification(title: String, body: String, pendingIntent: PendingIntent?) {
        // TODO: build and show reward notification
    }
}
