package com.example.unitask.data.room

import kotlinx.coroutines.flow.Flow

// Interfaz placeholder que imita un DAO de Room. Actualmente la persistencia se maneja
// con SharedPreferences, pero estos métodos reflejan la forma esperada de una implementación.
interface NotificationDaoPlaceholder {
    // Simula insertar un ajuste de notificación.
    suspend fun insertPlaceholder(entity: Any)

    // Simula consultar un ajuste por ID.
    suspend fun getPlaceholder(id: String): Any?

    // Simula observar todos los registros.
    fun observeAllPlaceholder(): Flow<List<Any>>

    // Simula eliminar un registro.
    suspend fun deletePlaceholder(entity: Any)
}
