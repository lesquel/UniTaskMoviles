package com.example.unitask.domain.usecase

import com.example.unitask.domain.repository.RewardRepository
import kotlinx.coroutines.flow.Flow

/**
 * Devuelve el flujo del nivel actual del usuario.
 */
class GetLevelUseCase(private val repo: RewardRepository) {
    operator fun invoke(): Flow<Int> = repo.getLevel()
}
