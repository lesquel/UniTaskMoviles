package com.example.unitask.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.util.Log

/**
 * Envoltorio de AlarmManager que permite simular las llamadas desde pruebas.
 */
interface AlarmManagerWrapper {
    fun setExactAndAllowWhileIdle(type: Int, triggerAtMillis: Long, intent: PendingIntent)
    fun setExact(type: Int, triggerAtMillis: Long, intent: PendingIntent)
    fun setAlarmClock(triggerAtMillis: Long, showIntent: PendingIntent, operationIntent: PendingIntent)
    fun setInexactRepeating(type: Int, triggerAtMillis: Long, intervalMillis: Long, intent: PendingIntent)
    fun cancel(intent: PendingIntent)
    fun canScheduleExactAlarms(): Boolean
}

class RealAlarmManagerWrapper(
    private val context: Context,
    private val alarmManager: AlarmManager
) : AlarmManagerWrapper {
    
    companion object {
        private const val TAG = "AlarmManagerWrapper"
    }
    
    override fun setExactAndAllowWhileIdle(type: Int, triggerAtMillis: Long, intent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(type, triggerAtMillis, intent)
                    Log.d(TAG, "Alarm scheduled with setExactAndAllowWhileIdle at $triggerAtMillis")
                } else {
                    // Fallback si no tiene permiso de alarmas exactas
                    alarmManager.set(type, triggerAtMillis, intent)
                    Log.w(TAG, "Using non-exact alarm as fallback at $triggerAtMillis")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(type, triggerAtMillis, intent)
                Log.d(TAG, "Alarm scheduled with setExactAndAllowWhileIdle at $triggerAtMillis")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling alarm, using fallback", e)
            alarmManager.set(type, triggerAtMillis, intent)
        }
    }

    override fun setExact(type: Int, triggerAtMillis: Long, intent: PendingIntent) {
        try {
            alarmManager.setExact(type, triggerAtMillis, intent)
            Log.d(TAG, "Alarm scheduled with setExact at $triggerAtMillis")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling exact alarm", e)
            alarmManager.set(type, triggerAtMillis, intent)
        }
    }
    
    override fun setAlarmClock(triggerAtMillis: Long, showIntent: PendingIntent, operationIntent: PendingIntent) {
        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent)
            alarmManager.setAlarmClock(alarmClockInfo, operationIntent)
            Log.d(TAG, "Alarm scheduled with setAlarmClock at $triggerAtMillis")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling alarm clock, using fallback", e)
            setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operationIntent)
        }
    }

    override fun setInexactRepeating(type: Int, triggerAtMillis: Long, intervalMillis: Long, intent: PendingIntent) {
        alarmManager.setInexactRepeating(type, triggerAtMillis, intervalMillis, intent)
        Log.d(TAG, "Repeating alarm scheduled at $triggerAtMillis with interval $intervalMillis")
    }

    override fun cancel(intent: PendingIntent) {
        alarmManager.cancel(intent)
        Log.d(TAG, "Alarm cancelled")
    }
    
    override fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
}

// Se puede agregar un fake en las fuentes de prueba si se necesita controlar el tiempo.
