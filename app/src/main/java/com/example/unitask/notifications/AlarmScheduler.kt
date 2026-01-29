package com.example.unitask.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build
import android.util.Log
import kotlin.math.max

/**
 * Agenda alarmas usando AlarmManager, delegando la lógica a un wrapper para facilitar pruebas.
 * Utiliza setAlarmClock para alarmas críticas que deben sonar siempre.
 */
class AlarmScheduler(private val alarmManagerWrapper: AlarmManagerWrapper) {
    
    companion object {
        private const val TAG = "AlarmScheduler"
    }
    
    /**
     * Programa una alarma exacta que se disparará aunque la app esté cerrada o en modo Doze.
     * Usa setAlarmClock para máxima confiabilidad en alarmas únicas.
     */
    fun scheduleExact(id: String, triggerAtMillis: Long, repeatIntervalMillis: Long?, intent: PendingIntent) {
        val currentTime = System.currentTimeMillis()
        
        // No programar alarmas en el pasado
        if (triggerAtMillis <= currentTime) {
            Log.w(TAG, "Alarm time is in the past ($triggerAtMillis vs $currentTime), skipping")
            return
        }
        
        Log.d(TAG, "Scheduling alarm $id for ${triggerAtMillis - currentTime}ms from now")
        
        if (repeatIntervalMillis == null) {
            // Para alarmas únicas, usar setAlarmClock que es el más confiable
            // y garantiza que la alarma se dispare incluso en modo Doze
            try {
                alarmManagerWrapper.setAlarmClock(triggerAtMillis, intent, intent)
            } catch (e: Exception) {
                Log.e(TAG, "setAlarmClock failed, falling back to setExactAndAllowWhileIdle", e)
                // Fallback a setExactAndAllowWhileIdle
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManagerWrapper.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, intent)
                } else {
                    alarmManagerWrapper.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, intent)
                }
            }
        } else {
            val interval = max(60_000L, repeatIntervalMillis)
            // Las repeticiones se ajustan a un mínimo de 1 minuto para no saturar el sistema.
            alarmManagerWrapper.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, interval, intent)
        }
    }

    fun cancel(id: String, intent: PendingIntent) {
        Log.d(TAG, "Cancelling alarm $id")
        // Cancelamos la alarma y el PendingIntent asociado para liberar recursos.
        alarmManagerWrapper.cancel(intent)
        try {
            intent.cancel()
        } catch (_: Exception) {}
    }
    
    /**
     * Verifica si la app tiene permiso para programar alarmas exactas.
     */
    fun canScheduleExactAlarms(): Boolean {
        return alarmManagerWrapper.canScheduleExactAlarms()
    }
}
