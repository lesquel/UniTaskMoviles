package com.example.unitask.data.repository

import com.example.unitask.domain.repository.RewardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// In-memory placeholder implementation to replace the old Room-backed repository.
class InMemoryRewardRepository : RewardRepository {
    private val xpFlow = MutableStateFlow(0)
    private val levelFlow = MutableStateFlow(1)

    override suspend fun addXp(amount: Int) {
        var newXp = xpFlow.value + amount
        var newLevel = levelFlow.value
        while (newXp >= newLevel * 100) {
            newXp -= newLevel * 100
            newLevel += 1
        }
        xpFlow.value = newXp
        levelFlow.value = newLevel
    }

    override fun getXp(): Flow<Int> = xpFlow

    override suspend fun resetXp() {
        xpFlow.value = 0
        levelFlow.value = 1
    }

    override fun getLevel(): Flow<Int> = levelFlow
}
