package com.example.unitask.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.unitask.domain.model.User
import java.time.LocalDateTime

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    val email: String,
    val passwordHash: String,
    val profileImagePath: String?,
    val totalXp: Int,
    val createdAt: LocalDateTime,
    val isLoggedIn: Boolean
) {
    fun toDomain(): User = User(
        id = id,
        username = username,
        email = email,
        passwordHash = passwordHash,
        profileImagePath = profileImagePath,
        totalXp = totalXp,
        createdAt = createdAt,
        isLoggedIn = isLoggedIn
    )

    companion object {
        fun fromDomain(user: User): UserEntity = UserEntity(
            id = user.id,
            username = user.username,
            email = user.email,
            passwordHash = user.passwordHash,
            profileImagePath = user.profileImagePath,
            totalXp = user.totalXp,
            createdAt = user.createdAt,
            isLoggedIn = user.isLoggedIn
        )
    }
}

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey
    val userId: String,
    val totalTasksCompleted: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastCompletedDate: LocalDateTime?
)
