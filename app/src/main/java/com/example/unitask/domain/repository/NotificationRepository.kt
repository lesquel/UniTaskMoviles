package com.example.unitask.domain.repository

import com.example.unitask.domain.model.NotificationSetting
import kotlinx.coroutines.flow.Flow

// Define cómo se deben gestionar los ajustes de recordatorios desde el dominio.
interface NotificationRepository {
    suspend fun save(setting: NotificationSetting)
    suspend fun delete(id: String)
    suspend fun get(id: String): NotificationSetting?
    fun observeAll(): Flow<List<NotificationSetting>>
    suspend fun schedule(setting: NotificationSetting)
    suspend fun cancel(id: String)
}
