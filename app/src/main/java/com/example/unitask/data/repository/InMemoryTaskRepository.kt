package com.example.unitask.data.repository

import com.example.unitask.domain.model.Task
import com.example.unitask.domain.repository.TaskRepository
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryTaskRepository(
    initialTasks: List<Task> = emptyList()
) : TaskRepository {

    // Comparador que prioriza fecha de entrega y luego título para mantener orden estable.
    private val taskComparator = compareBy<Task> { it.dueDateTime }.thenBy { it.title.lowercase() }
    private val _tasks = MutableStateFlow(initialTasks.sortedWith(taskComparator))

    // Flujo observable con las tareas ordenadas.
    override fun getTasksFlow(): Flow<List<Task>> = _tasks.asStateFlow()

    // Inserta una tarea nueva asegurando validez y que el ID no exista.
    override suspend fun addTask(task: Task) {
        validateTask(task)
        _tasks.update { current ->
            check(current.none { it.id == task.id }) { "Task id already exists." }
            (current + task).sortedWith(taskComparator)
        }
    }

    // Marca una tarea completa y reordena la lista.
    override suspend fun completeTask(taskId: String) {
        _tasks.update { current ->
            check(current.any { it.id == taskId }) { "Task not found." }
            current.map { task ->
                if (task.id == taskId) task.copy(isCompleted = true) else task
            }.sortedWith(taskComparator)
        }
    }

    // Borra una tarea por su identificador.
    override suspend fun deleteTask(taskId: String) {
        _tasks.update { current ->
            check(current.any { it.id == taskId }) { "Task not found." }
            current.filterNot { it.id == taskId }
        }
    }

    // Actualiza una tarea existente con nuevos datos.
    override suspend fun updateTask(task: Task) {
        validateTask(task)
        _tasks.update { current ->
            check(current.any { it.id == task.id }) { "Task not found." }
            current.map { if (it.id == task.id) task else it }
                .sortedWith(taskComparator)
        }
    }

    // Asegura que la tarea tenga título y fecha futura.
    private fun validateTask(task: Task) {
        require(task.title.isNotBlank()) { "Task title cannot be blank." }
        val now = LocalDateTime.now()
        require(!task.dueDateTime.isBefore(now)) {
            "Task due date must be now or in the future."
        }
    }

    companion object {
        // Fábrica para inicializar el repositorio con tareas de ejemplo.
        fun withSampleData(): InMemoryTaskRepository = InMemoryTaskRepository(SampleData.tasks())
    }
}
