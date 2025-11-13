package com.example.unitask.domain.repository

import kotlinx.coroutines.flow.Flow

interface RewardRepository {
    suspend fun addXp(amount: Int)
    fun getXp(): Flow<Int>
    suspend fun resetXp()
    fun getLevel(): Flow<Int>
}
