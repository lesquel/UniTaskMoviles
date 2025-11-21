package com.example.unitask.domain.usecase

import com.example.unitask.domain.model.Task
import com.example.unitask.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Obtiene un flujo con todas las tareas, opcionalmente excluyendo las completadas.
 */
class GetAllTasksUseCase(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(includeCompleted: Boolean = false): Flow<List<Task>> {
        return taskRepository.getTasksFlow().map { tasks ->
            if (includeCompleted) tasks else tasks.filterNot { it.isCompleted }
        }
    }
}
