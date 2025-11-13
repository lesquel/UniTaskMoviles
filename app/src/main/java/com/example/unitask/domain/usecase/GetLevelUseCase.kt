package com.example.unitask.domain.usecase

import com.example.unitask.domain.repository.RewardRepository
import kotlinx.coroutines.flow.Flow

class GetLevelUseCase(private val repo: RewardRepository) {
    operator fun invoke(): Flow<Int> = repo.getLevel()
}
