package com.example.unitask.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reward")
data class RewardEntity(
    @PrimaryKey val id: Int = 0,
    val xp: Int,
    val level: Int,
    val lastAwardedAt: Long
)
