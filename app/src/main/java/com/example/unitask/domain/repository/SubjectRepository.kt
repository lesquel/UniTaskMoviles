package com.example.unitask.domain.repository

import com.example.unitask.domain.model.Subject
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {
    fun getSubjectsFlow(): Flow<List<Subject>>
    suspend fun addSubject(subject: Subject)
    suspend fun editSubject(subject: Subject)
    suspend fun deleteSubject(subjectId: String)
}
