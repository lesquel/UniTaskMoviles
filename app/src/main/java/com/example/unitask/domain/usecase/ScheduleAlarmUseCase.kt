package com.example.unitask.domain.usecase

import com.example.unitask.domain.model.NotificationSetting
import com.example.unitask.domain.repository.NotificationRepository

/**
 * Guarda y programa un recordatorio mediante el repositorio de notificaciones.
 * Nota: save() ya programa la alarma internamente si está habilitada,
 * por lo que no es necesario llamar a schedule() por separado.
 */
class ScheduleAlarmUseCase(private val repo: NotificationRepository) {
    suspend operator fun invoke(setting: NotificationSetting): Result<Unit> {
        return try {
            // save() ya programa la alarma si setting.enabled es true
            repo.save(setting)
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
