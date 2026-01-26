package com.example.unitask.domain.repository

import com.example.unitask.domain.model.Task
import kotlinx.coroutines.flow.Flow

// Contrato para operaciones de tareas que debe implementar cualquier repositorio.
interface TaskRepository {
    fun getTasksFlow(): Flow<List<Task>>
    // Obtiene las tareas de un usuario específico.
    fun getTasksFlowByUserId(userId: String): Flow<List<Task>>
    // Inserta una tarea validando reglas de negocio (título/futuro).
    suspend fun addTask(task: Task)
    // Marca una tarea como completada.
    suspend fun completeTask(taskId: String)
    // Elimina una tarea por su identificador.
    suspend fun deleteTask(taskId: String)
    // Reemplaza los datos de una tarea existente.
    suspend fun updateTask(task: Task)
}
