package com.example.unitask.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import kotlin.math.max
import com.example.unitask.notifications.AlarmReceiver


class AlarmScheduler(private val context: Context, private val alarmManager: AlarmManager) {
    fun scheduleExact(id: String, triggerAtMillis: Long, repeatIntervalMillis: Long?, intent: PendingIntent) {
        if (repeatIntervalMillis == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, intent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, intent)
            }
        } else {
            // For repeating alarms, use setInexactRepeating to be battery friendly; if exact repeats required, schedule next manually in receiver
            val interval = max(60_000L, repeatIntervalMillis)
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, interval, intent)
        }
    }

    fun cancel(id: String, intent: PendingIntent) {
        alarmManager.cancel(intent)
        try {
            intent.cancel()
        } catch (_: Exception) {}
    }
}
