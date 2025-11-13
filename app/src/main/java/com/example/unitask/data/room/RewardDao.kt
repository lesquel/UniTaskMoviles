package com.example.unitask.data.room

import kotlinx.coroutines.flow.Flow

// Placeholder for Reward DAO. Use SharedPreferences-backed repository instead.
interface RewardDaoPlaceholder {
    fun getXpPlaceholder(): Flow<Int>
    fun getLevelPlaceholder(): Flow<Int>
    suspend fun getRewardPlaceholder(): Any?
    suspend fun upsertPlaceholder(entity: Any)
}
