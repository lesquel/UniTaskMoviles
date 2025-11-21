package com.example.unitask.domain.usecase

import com.example.unitask.domain.model.NotificationSetting
import com.example.unitask.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

/**
 * Expone un flujo con todas las configuraciones de recordatorios almacenadas.
 */
class GetAllNotificationsUseCase(private val repo: NotificationRepository) {
    operator fun invoke(): Flow<List<NotificationSetting>> = repo.observeAll()
}
