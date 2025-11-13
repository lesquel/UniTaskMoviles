package com.example.unitask.data.repository

import com.example.unitask.data.room.NotificationDao
import com.example.unitask.data.room.NotificationEntity
import com.example.unitask.domain.model.NotificationSetting
import com.example.unitask.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomNotificationRepository(private val dao: NotificationDao) : NotificationRepository {
    override suspend fun save(setting: NotificationSetting) {
        dao.insert(setting.toEntity())
    }

    override suspend fun delete(id: String) {
        dao.get(id)?.let { dao.delete(it) }
    }

    override suspend fun get(id: String): NotificationSetting? = dao.get(id)?.toDomain()

    override fun observeAll(): Flow<List<NotificationSetting>> = dao.observeAll().map { it.map { e -> e.toDomain() } }

    override suspend fun schedule(setting: NotificationSetting) {
        // Implementation will delegate to AlarmScheduler in the notifications package via DI
    }

    override suspend fun cancel(id: String) {
        // Implementation will delegate to AlarmScheduler in the notifications package via DI
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
