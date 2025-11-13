package com.example.unitask.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context

/**
 * A thin wrapper around Android's AlarmManager to make scheduling testable.
 */
interface AlarmManagerWrapper {
    fun setExactAndAllowWhileIdle(type: Int, triggerAtMillis: Long, intent: PendingIntent)
    fun setExact(type: Int, triggerAtMillis: Long, intent: PendingIntent)
    fun setInexactRepeating(type: Int, triggerAtMillis: Long, intervalMillis: Long, intent: PendingIntent)
    fun cancel(intent: PendingIntent)
}

class RealAlarmManagerWrapper(private val alarmManager: AlarmManager) : AlarmManagerWrapper {
    override fun setExactAndAllowWhileIdle(type: Int, triggerAtMillis: Long, intent: PendingIntent) {
        alarmManager.setExactAndAllowWhileIdle(type, triggerAtMillis, intent)
    }

    override fun setExact(type: Int, triggerAtMillis: Long, intent: PendingIntent) {
        alarmManager.setExact(type, triggerAtMillis, intent)
    }

    override fun setInexactRepeating(type: Int, triggerAtMillis: Long, intervalMillis: Long, intent: PendingIntent) {
        alarmManager.setInexactRepeating(type, triggerAtMillis, intervalMillis, intent)
    }

    override fun cancel(intent: PendingIntent) {
        alarmManager.cancel(intent)
    }
}

// A simple fake implementation for tests can be written in test sources when needed.
