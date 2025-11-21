package com.example.unitask.domain.model

// Representa la configuración persistida de un recordatorio/alarma.

data class NotificationSetting(
    val id: String,
    val taskId: String?,
    val enabled: Boolean,
    val triggerAtMillis: Long,
    val repeatIntervalMillis: Long?,
    val useMinutes: Boolean,
    val exact: Boolean
)
