package com.example.unitask.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NotificationEntity)

    @Query("SELECT * FROM notifications WHERE id = :id LIMIT 1")
    suspend fun get(id: String): NotificationEntity?

    @Query("SELECT * FROM notifications")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Delete
    suspend fun delete(entity: NotificationEntity)
}
