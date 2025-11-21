package com.example.unitask.domain.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * Modelo de dominio que describe una tarea académica planificada.
 */
data class Task(
    val id: String = UUID.randomUUID().toString(),               // Identificador único.
    val title: String,                                          // Título descriptivo de la tarea.
    val subjectId: String,                                      // ID de la asignatura asociada.
    val dueDateTime: LocalDateTime,                            // Fecha y hora de entrega.
    val createdAt: LocalDateTime = LocalDateTime.now(),        // Timestamp de creación.
    val isCompleted: Boolean = false                           // Indica si ya fue completada.
)
