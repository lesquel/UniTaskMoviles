package com.example.unitask.domain.usecase

import com.example.unitask.domain.model.NotificationSetting
import com.example.unitask.domain.repository.NotificationRepository

class ScheduleAlarmUseCase(private val repo: NotificationRepository) {
    suspend operator fun invoke(setting: NotificationSetting): Result<Unit> {
        return try {
            repo.save(setting)
            repo.schedule(setting)
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
