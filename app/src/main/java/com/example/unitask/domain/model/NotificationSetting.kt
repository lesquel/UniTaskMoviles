package com.example.unitask.domain.model

// Representa la configuración persistida de un recordatorio/alarma.
data class NotificationSetting(
    val id: String,                            // Identificador único del recordatorio.
    val taskId: String?,                      // Tarea asociada, si existe.
    val enabled: Boolean,                     // Indica si el recordatorio está activo.
    val triggerAtMillis: Long,                // Momento en que se debe disparar.
    val repeatIntervalMillis: Long?,          // Intervalo de repetición (opcional).
    val useMinutes: Boolean,                  // Si usa minutos en vez de horas.
    val exact: Boolean,                       // Si requiere alarma exacta.
    val taskTitle: String? = null,            // Título de la tarea para la notificación.
    val subjectName: String? = null           // Nombre de la materia para la notificación.
)
