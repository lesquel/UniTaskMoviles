package com.example.unitask.domain.model

data class NotificationSetting(
    val id: String,
    val taskId: String?,
    val enabled: Boolean,
    val triggerAtMillis: Long,
    val repeatIntervalMillis: Long?,
    val useMinutes: Boolean,
    val exact: Boolean
)
