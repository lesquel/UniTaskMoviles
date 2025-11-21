package com.example.unitask.domain.usecase

import com.example.unitask.domain.model.Subject
import com.example.unitask.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.first

/**
 * Edita una asignatura y garantiza nombres únicos y colores válidos.
 */
class EditSubjectUseCase(
    private val subjectRepository: SubjectRepository
) {
    suspend operator fun invoke(
        subjectId: String,
        name: String,
        colorHex: String,
        teacher: String? = null
    ): Subject {
        require(subjectId.isNotBlank()) { "Subject id cannot be blank." }
        val cleanedName = name.trim()
        require(cleanedName.isNotEmpty()) { "Subject name cannot be blank." }

        val normalizedColor = normalizeColor(colorHex)

        val subjects = subjectRepository.getSubjectsFlow().first()
        val existing = subjects.find { it.id == subjectId }
            ?: error("Subject not found.")

        check(subjects.none { it.id != subjectId && it.name.equals(cleanedName, ignoreCase = true) }) {
            "Another subject already uses that name."
        }

        val updated = existing.copy(
            name = cleanedName,
            colorHex = normalizedColor,
            teacher = teacher?.takeIf { it.isNotBlank() }
        )

        subjectRepository.editSubject(updated)
        return updated
    }

    private fun normalizeColor(input: String): String {
        val cleaned = input.trim().removePrefix("#")
        require(cleaned.length == 6 && cleaned.all { it in validHexChars }) {
            "Color must be a 6-digit hex value."
        }
        return "#${cleaned.uppercase()}"
    }

    private companion object {
        private val validHexChars = ('0'..'9') + ('A'..'F') + ('a'..'f')
    }
}
