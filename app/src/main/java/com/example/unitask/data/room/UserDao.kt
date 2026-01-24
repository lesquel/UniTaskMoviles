package com.example.unitask.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUser(): UserEntity?
    
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun observeLoggedInUser(): Flow<UserEntity?>
    
    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAllUsers()
    
    @Query("UPDATE users SET isLoggedIn = 1 WHERE id = :userId")
    suspend fun setUserLoggedIn(userId: String)
    
    @Query("UPDATE users SET totalXp = totalXp + :xp WHERE id = :userId")
    suspend fun addXp(userId: String, xp: Int)
    
    @Query("SELECT * FROM users ORDER BY totalXp DESC LIMIT :limit")
    suspend fun getTopUsersByXp(limit: Int): List<UserEntity>
    
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
    
    // User Stats
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStatsEntity)
    
    @Query("SELECT * FROM user_stats WHERE userId = :userId")
    suspend fun getUserStats(userId: String): UserStatsEntity?
    
    @Query("UPDATE user_stats SET totalTasksCompleted = totalTasksCompleted + 1, currentStreak = :newStreak, longestStreak = CASE WHEN :newStreak > longestStreak THEN :newStreak ELSE longestStreak END, lastCompletedDate = :completedDate WHERE userId = :userId")
    suspend fun incrementTasksCompleted(userId: String, newStreak: Int, completedDate: java.time.LocalDateTime)
}
