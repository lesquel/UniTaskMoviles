package com.example.unitask.domain.usecase

import com.example.unitask.domain.repository.TaskRepository

/**
 * Caso de uso para eliminar una tarea por su ID.
 */
class DeleteTaskUseCase(private val taskRepository: TaskRepository) {
    suspend operator fun invoke(taskId: String) {
        taskRepository.deleteTask(taskId)
    }
}
