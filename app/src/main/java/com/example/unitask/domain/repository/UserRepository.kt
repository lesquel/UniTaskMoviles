package com.example.unitask.domain.repository

import com.example.unitask.domain.model.SubjectInfo
import com.example.unitask.domain.model.User
import com.example.unitask.domain.model.UserStats
import com.example.unitask.presentation.viewmodel.UserRankingItem
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para gestión de usuarios.
 */
interface UserRepository {
    
    /**
     * Registra un nuevo usuario.
     */
    suspend fun register(username: String, email: String, password: String): Result<User>
    
    /**
     * Inicia sesión con credenciales.
     */
    suspend fun login(usernameOrEmail: String, password: String): Result<User>
    
    /**
     * Cierra la sesión del usuario actual.
     */
    suspend fun logout()
    
    /**
     * Obtiene el usuario actualmente logueado.
     */
    suspend fun getCurrentUser(): User?
    
    /**
     * Observa cambios en el usuario logueado.
     */
    fun observeCurrentUser(): Flow<User?>
    
    /**
     * Actualiza el perfil del usuario.
     */
    suspend fun updateProfile(
        userId: String,
        username: String,
        email: String,
        profileImagePath: String?
    )
    
    /**
     * Obtiene las estadísticas del usuario.
     */
    suspend fun getUserStats(userId: String): UserStats?
    
    /**
     * Incrementa el contador de tareas completadas.
     */
    suspend fun incrementTasksCompleted(userId: String)
    
    /**
     * Agrega XP al usuario.
     */
    suspend fun addXp(userId: String, xp: Int)
    
    /**
     * Obtiene el ranking de usuarios.
     */
    suspend fun getLeaderboard(subjectId: String?, limit: Int): List<UserRankingItem>
    
    /**
     * Obtiene las materias para los filtros del leaderboard.
     */
    suspend fun getSubjectsForFilters(): List<SubjectInfo>
    
    /**
     * Verifica si hay usuarios registrados.
     */
    suspend fun hasUsers(): Boolean
}
