package com.example.unitask.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context

class AlarmScheduler(private val context: Context, private val alarmManager: AlarmManager) {
    fun scheduleExact(id: String, triggerAtMillis: Long, repeatIntervalMillis: Long?, intent: PendingIntent) {
        // TODO: call alarmManager.setExactAndAllowWhileIdle or setInexactRepeating depending on repeatIntervalMillis
    }

    fun cancel(id: String, intent: PendingIntent) {
        // TODO: cancel pending intent
    }
}
