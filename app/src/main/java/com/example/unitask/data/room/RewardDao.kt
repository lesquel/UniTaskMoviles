package com.example.unitask.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {
    @Query("SELECT xp FROM reward LIMIT 1")
    fun getXp(): Flow<Int>

    @Query("SELECT level FROM reward LIMIT 1")
    fun getLevel(): Flow<Int>

    @Query("SELECT * FROM reward LIMIT 1")
    suspend fun getReward(): RewardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RewardEntity)
}
