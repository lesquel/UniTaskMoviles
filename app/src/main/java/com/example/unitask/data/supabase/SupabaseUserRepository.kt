package com.example.unitask.data.supabase

import com.example.unitask.domain.model.SubjectInfo
import com.example.unitask.domain.model.User
import com.example.unitask.domain.model.UserStats
import com.example.unitask.domain.repository.UserRepository
import com.example.unitask.presentation.viewmodel.UserRankingItem
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/**
 * Implementación de UserRepository usando Supabase para autenticación y datos.
 * 
 * NOTA: Esta implementación está preparada para cuando se migre completamente a Supabase.
 * Por ahora, la app usa RoomUserRepository como implementación principal.
 */
class SupabaseUserRepository : UserRepository {
    
    private val client = SupabaseClientProvider.client
    private val _currentUser = MutableStateFlow<User?>(null)
    
    // Modelos serializables para Supabase
    @Serializable
    data class ProfileDto(
        val id: String,
        val username: String,
        val email: String,
        val avatar_url: String? = null,
        val total_xp: Int = 0
    )
    
    @Serializable
    data class UserStatsDto(
        val user_id: String,
        val total_tasks_completed: Int = 0,
        val current_streak: Int = 0,
        val longest_streak: Int = 0,
        val last_completed_date: String? = null
    )
    
    override suspend fun register(username: String, email: String, password: String): Result<User> {
        return try {
            // Registrar con Supabase Auth
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            val authUser = client.auth.currentUserOrNull()
                ?: return Result.failure(Exception("Error en el registro"))
            
            // Crear perfil en la tabla profiles
            val profile = ProfileDto(
                id = authUser.id,
                username = username,
                email = email
            )
            
            client.postgrest.from("profiles").insert(profile)
            
            // Crear estadísticas iniciales
            val stats = UserStatsDto(user_id = authUser.id)
            client.postgrest.from("user_stats").insert(stats)
            
            val user = User(
                id = authUser.id,
                username = username,
                email = email,
                passwordHash = "", // Supabase maneja la autenticación
                profileImagePath = null,
                totalXp = 0,
                isLoggedIn = true
            )
            
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun login(usernameOrEmail: String, password: String): Result<User> {
        return try {
            // Supabase Auth usa email para login
            val email = if (usernameOrEmail.contains("@")) {
                usernameOrEmail
            } else {
                // Buscar email por username
                val result = client.postgrest
                    .from("profiles")
                    .select(columns = Columns.list("email")) {
                        filter {
                            eq("username", usernameOrEmail)
                        }
                    }
                    .decodeSingleOrNull<ProfileDto>()
                
                result?.email ?: return Result.failure(Exception("Usuario no encontrado"))
            }
            
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val authUser = client.auth.currentUserOrNull()
                ?: return Result.failure(Exception("Error en el login"))
            
            // Obtener perfil
            val profile = client.postgrest
                .from("profiles")
                .select {
                    filter {
                        eq("id", authUser.id)
                    }
                }
                .decodeSingleOrNull<ProfileDto>()
                ?: return Result.failure(Exception("Perfil no encontrado"))
            
            val user = User(
                id = profile.id,
                username = profile.username,
                email = profile.email,
                passwordHash = "",
                profileImagePath = profile.avatar_url,
                totalXp = profile.total_xp,
                isLoggedIn = true
            )
            
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun logout() {
        try {
            client.auth.signOut()
        } catch (_: Exception) { }
        _currentUser.value = null
    }
    
    override suspend fun getCurrentUser(): User? {
        if (_currentUser.value != null) return _currentUser.value
        
        val authUser = client.auth.currentUserOrNull() ?: return null
        
        return try {
            val profile = client.postgrest
                .from("profiles")
                .select {
                    filter {
                        eq("id", authUser.id)
                    }
                }
                .decodeSingleOrNull<ProfileDto>() ?: return null
            
            val user = User(
                id = profile.id,
                username = profile.username,
                email = profile.email,
                passwordHash = "",
                profileImagePath = profile.avatar_url,
                totalXp = profile.total_xp,
                isLoggedIn = true
            )
            
            _currentUser.value = user
            user
        } catch (_: Exception) {
            null
        }
    }
    
    override fun observeCurrentUser(): Flow<User?> = _currentUser
    
    override suspend fun updateProfile(
        userId: String,
        username: String,
        email: String,
        profileImagePath: String?
    ) {
        try {
            client.postgrest
                .from("profiles")
                .update({
                    set("username", username)
                    set("avatar_url", profileImagePath)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }
            
            _currentUser.value = _currentUser.value?.copy(
                username = username,
                profileImagePath = profileImagePath
            )
        } catch (_: Exception) { }
    }
    
    override suspend fun getUserStats(userId: String): UserStats? {
        return try {
            val stats = client.postgrest
                .from("user_stats")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<UserStatsDto>() ?: return null
            
            UserStats(
                userId = stats.user_id,
                totalTasksCompleted = stats.total_tasks_completed,
                currentStreak = stats.current_streak,
                longestStreak = stats.longest_streak
            )
        } catch (_: Exception) {
            null
        }
    }
    
    override suspend fun incrementTasksCompleted(userId: String) {
        try {
            val currentStats = client.postgrest
                .from("user_stats")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<UserStatsDto>()
            
            val newCount = (currentStats?.total_tasks_completed ?: 0) + 1
            
            client.postgrest
                .from("user_stats")
                .update({
                    set("total_tasks_completed", newCount)
                }) {
                    filter {
                        eq("user_id", userId)
                    }
                }
        } catch (_: Exception) { }
    }
    
    override suspend fun addXp(userId: String, amount: Int) {
        try {
            val profile = client.postgrest
                .from("profiles")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingleOrNull<ProfileDto>()
            
            val newXp = (profile?.total_xp ?: 0) + amount
            
            client.postgrest
                .from("profiles")
                .update({
                    set("total_xp", newXp)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }
            
            // Actualizar usuario en memoria
            _currentUser.value = _currentUser.value?.copy(totalXp = newXp)
        } catch (_: Exception) { }
    }
    
    override suspend fun getLeaderboard(subjectId: String?, limit: Int): List<UserRankingItem> {
        return try {
            val profiles = client.postgrest
                .from("profiles")
                .select()
                .decodeList<ProfileDto>()
            
            profiles.sortedByDescending { it.total_xp }
                .take(limit)
                .map { profile ->
                    val stats = getUserStats(profile.id)
                    UserRankingItem(
                        userId = profile.id,
                        username = profile.username,
                        profileImagePath = profile.avatar_url,
                        totalXp = profile.total_xp,
                        level = calculateLevel(profile.total_xp),
                        tasksCompleted = stats?.totalTasksCompleted ?: 0
                    )
                }
        } catch (_: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getSubjectsForFilters(): List<SubjectInfo> {
        // Implementación básica - se puede expandir más adelante
        return emptyList()
    }
    
    override suspend fun hasUsers(): Boolean {
        return try {
            val profiles = client.postgrest
                .from("profiles")
                .select()
                .decodeList<ProfileDto>()
            profiles.isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }
    
    private fun calculateLevel(xp: Int): Int {
        var level = 1
        var requiredXp = 100
        var totalRequired = requiredXp
        
        while (xp >= totalRequired) {
            level++
            requiredXp = level * 100
            totalRequired += requiredXp
        }
        
        return level
    }
}
