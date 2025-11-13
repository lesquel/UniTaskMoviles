package com.example.unitask.data.repository

import com.example.unitask.data.room.NotificationDao
import com.example.unitask.data.room.NotificationEntity
import com.example.unitask.domain.model.NotificationSetting
import com.example.unitask.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.unitask.notifications.AlarmScheduler
import android.content.Intent
import android.app.PendingIntent
import android.content.Context
import com.example.unitask.notifications.AlarmReceiver

class RoomNotificationRepository(private val dao: NotificationDao, private val alarmScheduler: AlarmScheduler, private val context: Context) : NotificationRepository {
    override suspend fun save(setting: NotificationSetting) {
        dao.insert(setting.toEntity())
        if (setting.enabled) {
            // schedule immediately when saved
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("alarm_id", setting.id)
                putExtra("task_id", setting.taskId)
            }
            val pending = PendingIntent.getBroadcast(context, setting.id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarmScheduler.scheduleExact(setting.id, setting.triggerAtMillis, setting.repeatIntervalMillis, pending)
        }
    }

    override suspend fun delete(id: String) {
        dao.get(id)?.let { dao.delete(it) }
    }

    override suspend fun get(id: String): NotificationSetting? = dao.get(id)?.toDomain()

    override fun observeAll(): Flow<List<NotificationSetting>> = dao.observeAll().map { it.map { e -> e.toDomain() } }

    override suspend fun schedule(setting: NotificationSetting) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", setting.id)
            putExtra("task_id", setting.taskId)
        }
        val pending = PendingIntent.getBroadcast(context, setting.id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmScheduler.scheduleExact(setting.id, setting.triggerAtMillis, setting.repeatIntervalMillis, pending)
    }

    override suspend fun cancel(id: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply { putExtra("alarm_id", id) }
        val pending = PendingIntent.getBroadcast(context, id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmScheduler.cancel(id, pending)
    }
}

private fun NotificationSetting.toEntity(): NotificationEntity = NotificationEntity(
    id = this.id,
    taskId = this.taskId,
    enabled = this.enabled,
    triggerAtMillis = this.triggerAtMillis,
    repeatIntervalMillis = this.repeatIntervalMillis,
    useMinutes = this.useMinutes,
    exact = this.exact
)

private fun com.example.unitask.data.room.NotificationEntity.toDomain(): NotificationSetting = NotificationSetting(
    id = this.id,
    taskId = this.taskId,
    enabled = this.enabled,
    triggerAtMillis = this.triggerAtMillis,
    repeatIntervalMillis = this.repeatIntervalMillis,
    useMinutes = this.useMinutes,
    exact = this.exact
)
