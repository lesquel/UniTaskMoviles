package com.example.unitask.notifications

import android.app.NotificationManager
import android.content.Context
import android.app.PendingIntent
import android.app.NotificationChannel
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Helper para crear canales y mostrar notificaciones configuradas con prioridades distintas.
 */
class NotificationHelper(private val context: Context, private val notificationManager: NotificationManager) {
    fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminder = NotificationChannel(
                "reminder",
                context.getString(com.example.unitask.R.string.notification_channel_reminder_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            reminder.description = context.getString(com.example.unitask.R.string.notification_channel_reminder_desc)
            val reward = NotificationChannel(
                "reward",
                context.getString(com.example.unitask.R.string.notification_channel_reward_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            reward.description = context.getString(com.example.unitask.R.string.notification_channel_reward_desc)
            notificationManager.createNotificationChannel(reminder)
            notificationManager.createNotificationChannel(reward)
        }
    }

    fun showReminderNotification(id: String, title: String, body: String, pendingIntent: PendingIntent?) {
        /**
         * Genera y publica una notificación de recordatorio con prioridad alta y comportamiento
         * de autocancelación, abriendo la app si el usuario la toca.
         */
        val notif = NotificationCompat.Builder(context, "reminder")
            .setSmallIcon(com.example.unitask.R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .apply { if (pendingIntent != null) setContentIntent(pendingIntent) }
            .build()
        NotificationManagerCompat.from(context).notify(id.hashCode(), notif)
    }

    fun showRewardNotification(title: String, body: String, pendingIntent: PendingIntent?) {
        /**
         * Publica notificación de recompensa en canal con prioridad normal, manteniendo la opción
         * de abrir la app si se provee un PendingIntent.
         */
        val notif = NotificationCompat.Builder(context, "reward")
            .setSmallIcon(com.example.unitask.R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .apply { if (pendingIntent != null) setContentIntent(pendingIntent) }
            .build()
        NotificationManagerCompat.from(context).notify(title.hashCode(), notif)
    }
}
