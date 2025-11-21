package com.example.unitask.domain.repository

import kotlinx.coroutines.flow.Flow

// Define la API para acumular XP y nivel dentro del dominio.
interface RewardRepository {
    suspend fun addXp(amount: Int)
    fun getXp(): Flow<Int>
    suspend fun resetXp()
    fun getLevel(): Flow<Int>
}
