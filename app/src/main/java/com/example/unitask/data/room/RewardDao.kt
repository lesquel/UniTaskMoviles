package com.example.unitask.data.room

import kotlinx.coroutines.flow.Flow

// DAO placeholder para recompensas. Describe qué métodos ofrecería una implementación real.
interface RewardDaoPlaceholder {
    // Observa XP desde la base de datos.
    fun getXpPlaceholder(): Flow<Int>

    // Observa el nivel actual.
    fun getLevelPlaceholder(): Flow<Int>

    // Obtiene la entidad de recompensa.
    suspend fun getRewardPlaceholder(): Any?

    // Inserta o actualiza una fila.
    suspend fun upsertPlaceholder(entity: Any)
}
