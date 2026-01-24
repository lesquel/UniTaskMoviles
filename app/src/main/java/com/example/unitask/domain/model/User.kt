package com.example.unitask.domain.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * Modelo de dominio para usuarios.
 */
data class User(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val email: String,
    val passwordHash: String,
    val profileImagePath: String? = null,
    val totalXp: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isLoggedIn: Boolean = false
)

/**
 * Estadísticas del usuario.
 */
data class UserStats(
    val userId: String,
    val totalTasksCompleted: Int = 0,
    val tasksByCategory: Map<String, Int> = emptyMap(), // subjectId -> count
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDate: LocalDateTime? = null
)

/**
 * Información de materia para filtros de leaderboard.
 */
data class SubjectInfo(
    val id: String,
    val name: String
)
