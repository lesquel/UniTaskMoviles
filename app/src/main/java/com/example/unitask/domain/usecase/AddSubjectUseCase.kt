package com.example.unitask.domain.usecase

import com.example.unitask.domain.model.Subject
import com.example.unitask.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.first

class AddSubjectUseCase(
    private val subjectRepository: SubjectRepository
) {
    suspend operator fun invoke(
        name: String,
        colorHex: String,
        teacher: String? = null
    ): Subject {
        val cleanedName = name.trim()
        require(cleanedName.isNotEmpty()) { "Subject name cannot be blank." }

        val normalizedColor = normalizeColor(colorHex)

        val existing = subjectRepository.getSubjectsFlow().first()
        check(existing.none { it.name.equals(cleanedName, ignoreCase = true) }) {
            "A subject with the same name already exists."
        }

        val subject = Subject(
            name = cleanedName,
            colorHex = normalizedColor,
            teacher = teacher?.takeIf { it.isNotBlank() }
        )
        subjectRepository.addSubject(subject)
        return subject
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
