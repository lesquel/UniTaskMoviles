package com.example.unitask.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * BroadcastReceiver que se activa con la alarma del sistema y restaura recordatorios al reiniciar.
 * Asegura que las notificaciones se muestren aunque la app esté cerrada o en segundo plano.
 */
class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "AlarmReceiver"
        private const val WAKE_LOCK_TIMEOUT = 10000L // 10 segundos
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive called with action: ${intent.action}")
        
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            // El sistema se reinició, volvemos a programar todas las alarmas existentes.
            Log.d(TAG, "Boot completed - rescheduling alarms")
            val req = OneTimeWorkRequestBuilder<RescheduleWorker>().build()
            WorkManager.getInstance(context).enqueue(req)
            return
        }

        val alarmId = intent.getStringExtra("alarm_id")
        if (alarmId == null) {
            Log.e(TAG, "Alarm ID is null, ignoring")
            return
        }
        
        Log.d(TAG, "Processing alarm: $alarmId")
        
        // Adquirir WakeLock para asegurar que el dispositivo no duerma durante la notificación
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "UniTask:AlarmWakeLock"
        )
        wakeLock.acquire(WAKE_LOCK_TIMEOUT)
        
        try {
            val taskId = intent.getStringExtra("task_id")
            val taskTitle = intent.getStringExtra("task_title") ?: "Tarea pendiente"
            val subjectName = intent.getStringExtra("subject_name") ?: ""
            
            Log.d(TAG, "Task: $taskTitle, Subject: $subjectName")
            
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
            
            // Mostrar notificación
            helper.showReminderNotification(alarmId, title, body, pending)
            Log.d(TAG, "Notification shown successfully")
            
            // Vibrar y reproducir sonido
            vibrateDevice(context)
            playAlarmSound(context)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing alarm", e)
        } finally {
            // Liberar WakeLock
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }
    
    private fun vibrateDevice(context: Context) {
        try {
            val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
            Log.d(TAG, "Device vibrated")
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating device", e)
        }
    }
    
    private fun playAlarmSound(context: Context) {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = false
                prepare()
                start()
            }
            
            // Detener después de 5 segundos
            android.os.Handler(context.mainLooper).postDelayed({
                try {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.stop()
                    }
                    mediaPlayer.release()
                } catch (e: Exception) {
                    // Ignorar
                }
            }, 5000)
            
            Log.d(TAG, "Alarm sound played")
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm sound", e)
            // Intentar con el Ringtone como fallback
            try {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val ringtone = RingtoneManager.getRingtone(context, alarmUri)
                ringtone?.play()
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback ringtone also failed", e2)
            }
        }
    }
}
