package com.example.unitask.domain.model

import java.util.UUID

/**
 * Modelo que describe una asignatura configurada por el estudiante.
 */
data class Subject(
    val id: String = UUID.randomUUID().toString(),                // UUID único.
    val name: String,                                             // Nombre legible.
    val colorHex: String,                                         // Color en formato hexadecimal.
    val teacher: String? = null                                   // Docente opcional.
)
