package com.example.unitask.domain.usecase

import com.example.unitask.domain.repository.NotificationRepository

/**
 * Cancela una alarma y elimina su configuración asociada.
 */
class CancelAlarmUseCase(private val repo: NotificationRepository) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return try {
            repo.cancel(id)
            repo.delete(id)
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
