package com.example.unitask.data.repository

import com.example.unitask.domain.model.Subject
import com.example.unitask.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemorySubjectRepository(
    initialSubjects: List<Subject> = emptyList()
) : SubjectRepository {

    private val _subjects = MutableStateFlow(initialSubjects.sortedBy(::normalizeName))

    override fun getSubjectsFlow(): Flow<List<Subject>> = _subjects.asStateFlow()

    override suspend fun addSubject(subject: Subject) {
        require(subject.name.isNotBlank()) { "Subject name cannot be blank." }
        _subjects.update { current ->
            check(current.none { it.id == subject.id }) { "Subject id already exists." }
            (current + subject).sortedBy(::normalizeName)
        }
    }

    override suspend fun editSubject(subject: Subject) {
        require(subject.name.isNotBlank()) { "Subject name cannot be blank." }
        _subjects.update { current ->
            check(current.any { it.id == subject.id }) { "Subject not found." }
            current.map { if (it.id == subject.id) subject else it }
                .sortedBy(::normalizeName)
        }
    }

    override suspend fun deleteSubject(subjectId: String) {
        _subjects.update { current ->
            check(current.any { it.id == subjectId }) { "Subject not found." }
            current.filterNot { it.id == subjectId }
        }
    }

    private fun normalizeName(subject: Subject): String = subject.name.trim().lowercase()

    companion object {
        fun withSampleData(): InMemorySubjectRepository = InMemorySubjectRepository(SampleData.subjects())
    }
}
