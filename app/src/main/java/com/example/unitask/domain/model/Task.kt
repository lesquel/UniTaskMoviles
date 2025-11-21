package com.example.unitask.domain.model

// Modelo que representa la información esencial de una tarea.

import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents an academic task scheduled for a specific subject.
 */
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subjectId: String,
    val dueDateTime: LocalDateTime,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isCompleted: Boolean = false
)
