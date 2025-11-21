package com.example.unitask.notifications

import android.app.AlarmManager
import android.app.PendingIntent

/**
 * Envoltorio de AlarmManager que permite simular las llamadas desde pruebas.
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

// Se puede agregar un fake en las fuentes de prueba si se necesita controlar el tiempo.
