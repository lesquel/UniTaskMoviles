package com.example.unitask.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder

/**
 * Servicio para manejar alarmas que necesitan ejecutarse en primer plano.
 * Esto asegura que las alarmas se disparen incluso cuando la app está cerrada.
 */
class AlarmService : Service() {
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { handleAlarm(it) }
        stopSelf(startId)
        return START_NOT_STICKY
    }
    
    private fun handleAlarm(intent: Intent) {
        val alarmId = intent.getStringExtra("alarm_id") ?: return
        val taskId = intent.getStringExtra("task_id")
        val taskTitle = intent.getStringExtra("task_title") ?: "Tarea pendiente"
        val subjectName = intent.getStringExtra("subject_name") ?: ""
        
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val helper = NotificationHelper(this, nm)
        helper.createChannels()
        
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
        
        helper.showReminderNotification(alarmId, title, body, pending)
    }
}
