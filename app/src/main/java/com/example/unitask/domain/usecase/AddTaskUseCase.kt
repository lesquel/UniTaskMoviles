package com.example.unitask.domain.usecase

import com.example.unitask.domain.model.Task
import com.example.unitask.domain.repository.SubjectRepository
import com.example.unitask.domain.repository.TaskRepository
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first

class AddTaskUseCase(
    private val taskRepository: TaskRepository,
    private val subjectRepository: SubjectRepository,
    private val nowProvider: () -> LocalDateTime = { LocalDateTime.now() }
) {
/**
 * Caso de uso para crear una tarea validando que exista la asignatura referenciada.
 */
    suspend operator fun invoke(
        title: String,
        subjectId: String,
        dueDateTime: LocalDateTime
    ): Task {
        val cleanedTitle = title.trim()
        require(cleanedTitle.isNotEmpty()) { "Task title cannot be blank." }
        require(subjectId.isNotBlank()) { "Subject id is required." }

        val now = nowProvider()
        require(!dueDateTime.isBefore(now)) { "Due date must be now or in the future." }

        val subjectExists = subjectRepository.getSubjectsFlow()
            .first()
            .any { it.id == subjectId }
        check(subjectExists) { "Subject not found." }

        val task = Task(
            title = cleanedTitle,
            subjectId = subjectId,
            dueDateTime = dueDateTime,
            createdAt = now
        )

        taskRepository.addTask(task)
        return task
    }
}
