package com.example.unitask.domain.usecase

import com.example.unitask.domain.repository.RewardRepository
import kotlinx.coroutines.flow.Flow

/**
 * Expone el flujo de XP actual desde el repositorio.
 */
class GetXpUseCase(private val repo: RewardRepository) {
    operator fun invoke(): Flow<Int> = repo.getXp()
}
