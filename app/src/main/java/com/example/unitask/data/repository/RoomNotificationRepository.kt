package com.example.unitask.data.repository

import android.content.Context
import com.example.unitask.domain.model.NotificationSetting
import com.example.unitask.domain.repository.NotificationRepository

// Adapter that delegates to the SharedPrefsNotificationRepository implementation.
class RoomNotificationRepository(private val context: Context, private val alarmScheduler: com.example.unitask.notifications.AlarmScheduler) : NotificationRepository {
    private val delegate = SharedPrefsNotificationRepository(context, alarmScheduler)

    override suspend fun save(setting: NotificationSetting) = delegate.save(setting)

    override suspend fun delete(id: String) = delegate.delete(id)

    override suspend fun get(id: String): NotificationSetting? = delegate.get(id)

    override fun observeAll() = delegate.observeAll()

    override suspend fun schedule(setting: NotificationSetting) = delegate.schedule(setting)

    override suspend fun cancel(id: String) = delegate.cancel(id)
}
