package com.example.unitask.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * Servicio para manejar alarmas que necesitan ejecutarse en primer plano.
 * Esto asegura que las alarmas se disparen incluso cuando la app está cerrada.
 */
class AlarmService : Service() {
    
    companion object {
        private const val TAG = "AlarmService"
        private const val FOREGROUND_NOTIFICATION_ID = 9999
        
        fun startAlarmService(context: Context, intent: Intent) {
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtras(intent)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
    
    private var wakeLock: PowerManager.WakeLock? = null
    private var mediaPlayer: MediaPlayer? = null
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AlarmService created")
        
        // Adquirir WakeLock
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "UniTask:AlarmServiceWakeLock"
        )
        wakeLock?.acquire(30000L) // 30 segundos máximo
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "AlarmService onStartCommand")
        
        // Crear notificación de foreground inmediatamente
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val helper = NotificationHelper(this, nm)
        helper.createChannels()
        
        // Notificación de foreground service (requerida para Android O+)
        // En Android 14+ (API 34), los servicios foreground requieren un tipo específico
        // Para compatibilidad, solo usamos foreground en versiones anteriores a Android 14
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < 34) {
            val foregroundNotification = NotificationCompat.Builder(this, NotificationHelper.REMINDER_CHANNEL_ID)
                .setSmallIcon(com.example.unitask.R.mipmap.ic_launcher)
                .setContentTitle("UniTask")
                .setContentText("Procesando alarma...")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build()
            
            try {
                startForeground(FOREGROUND_NOTIFICATION_ID, foregroundNotification)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting foreground", e)
            }
        }
        
        intent?.let { handleAlarm(it) }
        
        // Detener el servicio después de procesar
        android.os.Handler(mainLooper).postDelayed({
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < 34) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
            stopSelf(startId)
        }, 5500L) // Esperar a que termine el sonido
        
        return START_NOT_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "AlarmService destroyed")
        
        // Liberar recursos
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaPlayer", e)
        }
        
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing WakeLock", e)
        }
    }
    
    private fun handleAlarm(intent: Intent) {
        val alarmId = intent.getStringExtra("alarm_id")
        if (alarmId == null) {
            Log.e(TAG, "Alarm ID is null")
            return
        }
        
        val taskId = intent.getStringExtra("task_id")
        val taskTitle = intent.getStringExtra("task_title") ?: "Tarea pendiente"
        val subjectName = intent.getStringExtra("subject_name") ?: ""
        
        Log.d(TAG, "Processing alarm: $alarmId, task: $taskTitle")
        
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val helper = NotificationHelper(this, nm)
        
        // Preparar intento para abrir la app
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("task_id", taskId)
        }
        
        val pending = PendingIntent.getActivity(
            this,
            alarmId.hashCode(),
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val title = "📚 Recordatorio: $taskTitle"
        val body = if (subjectName.isNotBlank()) {
            "Materia: $subjectName\n¡No olvides completar esta tarea!"
        } else {
            "¡No olvides completar esta tarea!"
        }
        
        // Mostrar notificación
        helper.showReminderNotification(alarmId, title, body, pending)
        Log.d(TAG, "Notification shown")
        
        // Vibrar
        vibrateDevice()
        
        // Reproducir sonido
        playAlarmSound()
    }
    
    private fun vibrateDevice() {
        try {
            val pattern = longArrayOf(0, 500, 200, 500, 200, 500, 200, 500)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
            Log.d(TAG, "Device vibrated")
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating", e)
        }
    }
    
    private fun playAlarmSound() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmService, alarmUri)
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
            android.os.Handler(mainLooper).postDelayed({
                try {
                    mediaPlayer?.let {
                        if (it.isPlaying) {
                            it.stop()
                        }
                        it.release()
                    }
                    mediaPlayer = null
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping media player", e)
                }
            }, 5000L)
            
            Log.d(TAG, "Alarm sound playing")
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm sound", e)
            // Fallback: usar Ringtone
            try {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val ringtone = RingtoneManager.getRingtone(this, alarmUri)
                ringtone?.play()
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback also failed", e2)
            }
        }
    }
}
