package com.example.unitask.notifications

import android.app.NotificationManager
import android.content.Context
import android.app.PendingIntent
import android.app.NotificationChannel
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Helper para crear canales y mostrar notificaciones configuradas con prioridades distintas.
 */
class NotificationHelper(private val context: Context, private val notificationManager: NotificationManager) {
    
    companion object {
        const val REMINDER_CHANNEL_ID = "reminder"
        const val REWARD_CHANNEL_ID = "reward"
    }
    
    fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal de recordatorios con sonido de alarma y vibración
            val reminder = NotificationChannel(
                REMINDER_CHANNEL_ID,
                context.getString(com.example.unitask.R.string.notification_channel_reminder_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(com.example.unitask.R.string.notification_channel_reminder_desc)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                // Configurar sonido de alarma
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
            }
            
            // Canal de recompensas con sonido de notificación normal
            val reward = NotificationChannel(
                REWARD_CHANNEL_ID,
                context.getString(com.example.unitask.R.string.notification_channel_reward_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(com.example.unitask.R.string.notification_channel_reward_desc)
                enableVibration(true)
            }
            
            notificationManager.createNotificationChannel(reminder)
            notificationManager.createNotificationChannel(reward)
        }
    }
    
    /**
     * Verifica si la app tiene permiso para mostrar notificaciones.
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showReminderNotification(id: String, title: String, body: String, pendingIntent: PendingIntent?) {
        // Verificar permiso antes de mostrar
        if (!hasNotificationPermission()) {
            return
        }
        
        val notif = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(com.example.unitask.R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .apply { 
                if (pendingIntent != null) setContentIntent(pendingIntent)
                // Sonido de alarma
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            }
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(id.hashCode(), notif)
        } catch (e: SecurityException) {
            // Permiso denegado, ignorar silenciosamente
        }
    }

    fun showRewardNotification(title: String, body: String, pendingIntent: PendingIntent?) {
        // Verificar permiso antes de mostrar
        if (!hasNotificationPermission()) {
            return
        }
        
        val notif = NotificationCompat.Builder(context, REWARD_CHANNEL_ID)
            .setSmallIcon(com.example.unitask.R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .apply { if (pendingIntent != null) setContentIntent(pendingIntent) }
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(title.hashCode(), notif)
        } catch (e: SecurityException) {
            // Permiso denegado, ignorar silenciosamente
        }
    }
}
