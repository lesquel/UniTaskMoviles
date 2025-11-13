package com.example.unitask.domain.usecase

import com.example.unitask.domain.repository.RewardRepository

class AwardXpUseCase(private val repo: RewardRepository) {
    suspend operator fun invoke(xp: Int): Result<Unit> {
        return try {
            repo.addXp(xp)
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
