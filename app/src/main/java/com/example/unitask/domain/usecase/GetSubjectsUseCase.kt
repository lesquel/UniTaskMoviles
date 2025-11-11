package com.example.unitask.domain.usecase

import com.example.unitask.domain.model.Subject
import com.example.unitask.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow

class GetSubjectsUseCase(
    private val subjectRepository: SubjectRepository
) {
    operator fun invoke(): Flow<List<Subject>> = subjectRepository.getSubjectsFlow()
}
