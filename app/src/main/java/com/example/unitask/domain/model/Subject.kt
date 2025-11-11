package com.example.unitask.domain.model

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
