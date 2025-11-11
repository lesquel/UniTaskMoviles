package com.example.unitask.domain.usecase

import com.example.unitask.domain.repository.SubjectRepository
import com.example.unitask.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first

class DeleteSubjectUseCase(
    private val subjectRepository: SubjectRepository,
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(subjectId: String, cascade: Boolean = true) {
        require(subjectId.isNotBlank()) { "Subject id cannot be blank." }

        val relatedTasks = taskRepository.getTasksFlow()
            .first()
            .filter { it.subjectId == subjectId }

        if (relatedTasks.isNotEmpty() && !cascade) {
            error("Cannot delete subject because it still has tasks assigned.")
        }

        relatedTasks.forEach { taskRepository.deleteTask(it.id) }
        subjectRepository.deleteSubject(subjectId)
    }
}
