package com.example.unitask.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import android.app.PendingIntent

/**
 * BroadcastReceiver que se activa con la alarma del sistema y restaura recordatorios al reiniciar.
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            // El sistema se reinició, volvemos a programar todas las alarmas existentes.
            val req = OneTimeWorkRequestBuilder<RescheduleWorker>().build()
            WorkManager.getInstance(context).enqueue(req)
            return
        }

        val alarmId = intent.getStringExtra("alarm_id") ?: return
        val taskId = intent.getStringExtra("task_id")
        val taskTitle = intent.getStringExtra("task_title") ?: "Tarea pendiente"
        val subjectName = intent.getStringExtra("subject_name") ?: ""
        
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val helper = NotificationHelper(context, nm)
        helper.createChannels()

        // Preparar intento para abrir la app si el usuario toca la notificación.
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("task_id", taskId)
        }
        
        val pending = PendingIntent.getActivity(
            context,
            alarmId.hashCode(),
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Título y cuerpo de la notificación
        val title = "📚 Recordatorio: $taskTitle"
        val body = if (subjectName.isNotBlank()) {
            "Materia: $subjectName\n¡No olvides completar esta tarea!"
        } else {
            "¡No olvides completar esta tarea!"
        }
        
        helper.showReminderNotification(alarmId, title, body, pending)
        
        // Vibrar el dispositivo para alertar
        vibrateDevice(context)
    }
    
    private fun vibrateDevice(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(500)
                }
            }
        } catch (e: Exception) {
            // Ignorar si no se puede vibrar
        }
    }
}
