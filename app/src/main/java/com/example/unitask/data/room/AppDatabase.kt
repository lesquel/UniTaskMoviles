package com.example.unitask.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [NotificationEntity::class, RewardEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun rewardDao(): RewardDao
}
