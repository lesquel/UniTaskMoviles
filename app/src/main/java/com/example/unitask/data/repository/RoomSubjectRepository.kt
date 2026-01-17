package com.example.unitask.data.repository

import com.example.unitask.data.room.SubjectDao
import com.example.unitask.data.room.toDomain
import com.example.unitask.data.room.toEntity
import com.example.unitask.domain.model.Subject
import com.example.unitask.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * [SubjectRepository] implementation using Room.
 * Handles persistence for Subjects (Asignaturas).
 */
class RoomSubjectRepository(private val subjectDao: SubjectDao) : SubjectRepository {

    /**
     * Observes the subjects table and maps [SubjectEntity] list to [Subject] list.
     */
    override fun getSubjectsFlow(): Flow<List<Subject>> {
        return subjectDao.getAllSubjects().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addSubject(subject: Subject) {
        subjectDao.insertSubject(subject.toEntity())
    }

    override suspend fun editSubject(subject: Subject) {
        subjectDao.insertSubject(subject.toEntity())
    }

    override suspend fun deleteSubject(subjectId: String) {
        val entity = subjectDao.getSubjectById(subjectId)
        entity?.let {
            subjectDao.deleteSubject(it)
        }
    }
}
