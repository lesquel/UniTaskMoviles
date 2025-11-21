package com.example.unitask.domain.usecase

import com.example.unitask.domain.model.Task
import com.example.unitask.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

/**
 * Busca una tarea concreta por su ID desde el repositorio.
 */
class GetTaskByIdUseCase(private val taskRepository: TaskRepository) {
    suspend operator fun invoke(taskId: String): Task? {
        return taskRepository.getTasksFlow()
            .first()
            .firstOrNull { it.id == taskId }
    }
}