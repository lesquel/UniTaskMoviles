package com.example.unitask.data.repository

import com.example.unitask.data.room.RewardDao
import com.example.unitask.data.room.RewardEntity
import com.example.unitask.domain.repository.RewardRepository
import kotlinx.coroutines.flow.Flow

class RoomRewardRepository(private val dao: RewardDao) : RewardRepository {
    override suspend fun addXp(amount: Int) {
        val current = dao.getReward()
        val now = System.currentTimeMillis()
        val (baseXp, baseLevel) = if (current == null) Pair(0, 1) else Pair(current.xp, current.level)
        var newXp = baseXp + amount
        var newLevel = baseLevel
        // Level requirement: level * 100 XP
        while (newXp >= newLevel * 100) {
            newXp -= newLevel * 100
            newLevel += 1
        }
        val entity = RewardEntity(id = 0, xp = newXp, level = newLevel, lastAwardedAt = now)
        dao.upsert(entity)
    }

    override fun getXp(): Flow<Int> = dao.getXp()

    override suspend fun resetXp() {
        dao.upsert(RewardEntity(xp = 0, level = 1, lastAwardedAt = System.currentTimeMillis()))
    }

    override fun getLevel(): Flow<Int> = dao.getLevel()
}
