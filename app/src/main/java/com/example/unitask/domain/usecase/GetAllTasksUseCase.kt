package com.example.unitask.domain.usecase

import com.example.unitask.domain.model.Task
import com.example.unitask.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Obtiene un flujo con todas las tareas, opcionalmente excluyendo las completadas.
 * Si se proporciona un userId, solo retorna las tareas del usuario.
 */
class GetAllTasksUseCase(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(userId: String? = null, includeCompleted: Boolean = false): Flow<List<Task>> {
        val tasksFlow = if (userId != null) {
            taskRepository.getTasksFlowByUserId(userId)
        } else {
            taskRepository.getTasksFlow()
        }
        return tasksFlow.map { tasks ->
            if (includeCompleted) tasks else tasks.filterNot { it.isCompleted }
        }
    }
}
