package com.example.unitask.notifications

import android.app.PendingIntent
import android.os.Build
import kotlin.math.max

/**
 * Agenda alarmas usando AlarmManager, delegando la lógica a un wrapper para facilitar pruebas.
 */
class AlarmScheduler(private val alarmManagerWrapper: AlarmManagerWrapper) {
    fun scheduleExact(id: String, triggerAtMillis: Long, repeatIntervalMillis: Long?, intent: PendingIntent) {
        // Si no hay intervalo repetido se agenda exacta (respetando Doze en Android M+).
        if (repeatIntervalMillis == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManagerWrapper.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, intent)
            } else {
                alarmManagerWrapper.setExact(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, intent)
            }
        } else {
            val interval = max(60_000L, repeatIntervalMillis)
            // Las repeticiones se ajustan a un mínimo de 1 minuto para no saturar el sistema.
            alarmManagerWrapper.setInexactRepeating(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, interval, intent)
        }
    }

    fun cancel(id: String, intent: PendingIntent) {
        // Cancelamos la alarma y el PendingIntent asociado para liberar recursos.
        alarmManagerWrapper.cancel(intent)
        try {
            intent.cancel()
        } catch (_: Exception) {}
    }
}
