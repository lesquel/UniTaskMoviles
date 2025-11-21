package com.example.unitask.domain.model

// Modelo que describe una asignatura con color y docente.

import java.util.UUID

/**
 * Represents a course configured by the student. Color is stored in hex format (#RRGGBB).
 */
data class Subject(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val colorHex: String,
    val teacher: String? = null
)
