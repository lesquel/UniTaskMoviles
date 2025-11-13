package com.example.unitask.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val taskId: String?,
    val enabled: Boolean,
    val triggerAtMillis: Long,
    val repeatIntervalMillis: Long?,
    val useMinutes: Boolean,
    val exact: Boolean
)
