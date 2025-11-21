package com.example.unitask.data.room

// Entidad placeholder: describe qué campos tendría la tabla de notificaciones si usaramos Room.
// Actualmente se persiste con SharedPreferences, pero mantenemos la definición para referencia.
data class NotificationEntityPlaceholder(
    val id: String = "",
    val taskId: String? = null,
    val enabled: Boolean = false,
    val triggerAtMillis: Long = 0L,
    val repeatIntervalMillis: Long? = null,
    val useMinutes: Boolean = false,
    val exact: Boolean = false
)
