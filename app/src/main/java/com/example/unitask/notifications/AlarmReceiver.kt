package com.example.unitask.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.unitask.notifications.NotificationHelper
import android.app.PendingIntent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            // enqueue reschedule worker
            val req = OneTimeWorkRequestBuilder<RescheduleWorker>().build()
            WorkManager.getInstance(context).enqueue(req)
            return
        }

        val alarmId = intent.getStringExtra("alarm_id") ?: return
        val taskId = intent.getStringExtra("task_id")
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val helper = NotificationHelper(context, nm)
        helper.createChannels()

        // Build pending intent to open app on tap
        val pending = PendingIntent.getActivity(context, alarmId.hashCode(), context.packageManager.getLaunchIntentForPackage(context.packageName), PendingIntent.FLAG_IMMUTABLE)
        val title = context.getString(com.example.unitask.R.string.app_name)
        val body = if (taskId != null) context.getString(com.example.unitask.R.string.add_task) else context.getString(com.example.unitask.R.string.add_task)
        helper.showReminderNotification(alarmId, title, body, pending)
    }
}
