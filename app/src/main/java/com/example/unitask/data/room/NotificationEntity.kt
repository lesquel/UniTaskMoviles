package com.example.unitask.data.room

// Placeholder for NotificationEntity. Persistence now uses SharedPreferences.
data class NotificationEntityPlaceholder(
    val id: String = "",
    val taskId: String? = null,
    val enabled: Boolean = false,
    val triggerAtMillis: Long = 0L,
    val repeatIntervalMillis: Long? = null,
    val useMinutes: Boolean = false,
    val exact: Boolean = false
)
