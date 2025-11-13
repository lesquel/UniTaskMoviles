package com.example.unitask.notifications

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import kotlin.math.max

/**
 * Use an AlarmManagerWrapper to allow tests to inject a fake.
 */
class AlarmScheduler(private val context: Context, private val alarmManagerWrapper: AlarmManagerWrapper) {
    fun scheduleExact(id: String, triggerAtMillis: Long, repeatIntervalMillis: Long?, intent: PendingIntent) {
        if (repeatIntervalMillis == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManagerWrapper.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, intent)
            } else {
                alarmManagerWrapper.setExact(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, intent)
            }
        } else {
            val interval = max(60_000L, repeatIntervalMillis)
            alarmManagerWrapper.setInexactRepeating(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, interval, intent)
        }
    }

    fun cancel(id: String, intent: PendingIntent) {
        alarmManagerWrapper.cancel(intent)
        try {
            intent.cancel()
        } catch (_: Exception) {}
    }
}
