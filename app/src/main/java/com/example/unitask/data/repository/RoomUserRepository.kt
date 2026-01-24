package com.example.unitask.data.repository

import com.example.unitask.data.room.SubjectDao
import com.example.unitask.data.room.UserDao
import com.example.unitask.data.room.UserEntity
import com.example.unitask.data.room.UserStatsEntity
import com.example.unitask.domain.model.SubjectInfo
import com.example.unitask.domain.model.User
import com.example.unitask.domain.model.UserStats
import com.example.unitask.domain.repository.UserRepository
import com.example.unitask.presentation.viewmodel.UserRankingItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.time.LocalDate
import java.time.LocalDateTime

class RoomUserRepository(
    private val userDao: UserDao,
    private val subjectDao: SubjectDao
) : UserRepository {

    override suspend fun register(username: String, email: String, password: String): Result<User> {
        return try {
            // Verificar si ya existe el usuario
            if (userDao.getUserByUsername(username) != null) {
                return Result.failure(Exception("El nombre de usuario ya existe"))
            }
            if (userDao.getUserByEmail(email) != null) {
                return Result.failure(Exception("El correo ya está registrado"))
            }
            
            val user = User(
                username = username,
                email = email,
                passwordHash = hashPassword(password),
                isLoggedIn = true
            )
            
            // Logout de cualquier usuario previo
            userDao.logoutAllUsers()
            
            // Insertar nuevo usuario
            userDao.insertUser(UserEntity.fromDomain(user))
            
            // Crear estadísticas iniciales
            userDao.insertUserStats(
                UserStatsEntity(
                    userId = user.id,
                    totalTasksCompleted = 0,
                    currentStreak = 0,
                    longestStreak = 0,
                    lastCompletedDate = null
                )
            )
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(usernameOrEmail: String, password: String): Result<User> {
        return try {
            val userEntity = userDao.getUserByUsername(usernameOrEmail)
                ?: userDao.getUserByEmail(usernameOrEmail)
                ?: return Result.failure(Exception("Usuario no encontrado"))
            
            val passwordHash = hashPassword(password)
            if (userEntity.passwordHash != passwordHash) {
                return Result.failure(Exception("Contraseña incorrecta"))
            }
            
            // Logout de otros usuarios y login este
            userDao.logoutAllUsers()
            userDao.setUserLoggedIn(userEntity.id)
            
            Result.success(userEntity.toDomain().copy(isLoggedIn = true))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        userDao.logoutAllUsers()
    }

    override suspend fun getCurrentUser(): User? {
        return userDao.getLoggedInUser()?.toDomain()
    }

    override fun observeCurrentUser(): Flow<User?> {
        return userDao.observeLoggedInUser().map { it?.toDomain() }
    }

    override suspend fun updateProfile(
        userId: String,
        username: String,
        email: String,
        profileImagePath: String?
    ) {
        val existingUser = userDao.getUserById(userId) ?: return
        val updatedUser = existingUser.copy(
            username = username,
            email = email,
            profileImagePath = profileImagePath
        )
        userDao.updateUser(updatedUser)
    }

    override suspend fun getUserStats(userId: String): UserStats? {
        val statsEntity = userDao.getUserStats(userId) ?: return null
        return UserStats(
            userId = statsEntity.userId,
            totalTasksCompleted = statsEntity.totalTasksCompleted,
            currentStreak = statsEntity.currentStreak,
            longestStreak = statsEntity.longestStreak,
            lastCompletedDate = statsEntity.lastCompletedDate
        )
    }

    override suspend fun incrementTasksCompleted(userId: String) {
        val stats = userDao.getUserStats(userId)
        val today = LocalDate.now()
        val lastCompletedDate = stats?.lastCompletedDate?.toLocalDate()
        
        val newStreak = when {
            lastCompletedDate == null -> 1
            lastCompletedDate == today -> stats.currentStreak // Ya completó una hoy
            lastCompletedDate == today.minusDays(1) -> stats.currentStreak + 1 // Día consecutivo
            else -> 1 // Reiniciar racha
        }
        
        if (stats == null) {
            userDao.insertUserStats(
                UserStatsEntity(
                    userId = userId,
                    totalTasksCompleted = 1,
                    currentStreak = 1,
                    longestStreak = 1,
                    lastCompletedDate = LocalDateTime.now()
                )
            )
        } else {
            userDao.incrementTasksCompleted(userId, newStreak, LocalDateTime.now())
        }
    }

    override suspend fun addXp(userId: String, xp: Int) {
        userDao.addXp(userId, xp)
    }

    override suspend fun getLeaderboard(subjectId: String?, limit: Int): List<UserRankingItem> {
        val users = userDao.getTopUsersByXp(limit)
        return users.map { user ->
            val stats = userDao.getUserStats(user.id)
            UserRankingItem(
                userId = user.id,
                username = user.username,
                profileImagePath = user.profileImagePath,
                totalXp = user.totalXp,
                level = calculateLevel(user.totalXp),
                tasksCompleted = stats?.totalTasksCompleted ?: 0
            )
        }
    }

    override suspend fun getSubjectsForFilters(): List<SubjectInfo> {
        return subjectDao.getAllOnce().map { subject ->
            SubjectInfo(
                id = subject.id,
                name = subject.name
            )
        }
    }

    override suspend fun hasUsers(): Boolean {
        return userDao.getUserCount() > 0
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
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
