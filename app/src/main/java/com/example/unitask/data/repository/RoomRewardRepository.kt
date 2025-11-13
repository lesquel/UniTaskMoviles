package com.example.unitask.data.repository

import com.example.unitask.data.room.RewardDao
import com.example.unitask.data.room.RewardEntity
import com.example.unitask.domain.repository.RewardRepository
import kotlinx.coroutines.flow.Flow

class RoomRewardRepository(private val dao: RewardDao) : RewardRepository {
    override suspend fun addXp(amount: Int) {
        // Simple upsert logic should be implemented: read current, add xp, compute level, persist
    }

    override fun getXp(): Flow<Int> = dao.getXp()

    override suspend fun resetXp() {
        dao.upsert(RewardEntity(xp = 0, level = 1, lastAwardedAt = System.currentTimeMillis()))
    }

    override fun getLevel(): Flow<Int> = dao.getLevel()
}
