package com.example.unitask.data.room

import kotlinx.coroutines.flow.Flow

// Placeholder DAO-like interface. Real implementations use SharedPreferences.
interface NotificationDaoPlaceholder {
    suspend fun insertPlaceholder(entity: Any)
    suspend fun getPlaceholder(id: String): Any?
    fun observeAllPlaceholder(): Flow<List<Any>>
    suspend fun deletePlaceholder(entity: Any)
}
