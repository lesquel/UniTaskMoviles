package com.example.unitask.data.supabase

import com.example.unitask.domain.model.Task
import com.example.unitask.domain.repository.TaskRepository
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Implementación de TaskRepository usando Supabase Postgrest.
 */
class SupabaseTaskRepository : TaskRepository {
    
    private val client = SupabaseClientProvider.client
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    
    @Serializable
    data class TaskDto(
        val id: String,
        val user_id: String,
        val subject_id: String,
        val title: String,
        val due_date_time: String,
        val is_completed: Boolean = false,
        val created_at: String? = null
    )
    
    private val dateFormatter = DateTimeFormatter.ISO_DATE_TIME
    
    override fun getTasksFlow(): Flow<List<Task>> {
        return _tasks
    }
    
    override fun getTasksFlowByUserId(userId: String): Flow<List<Task>> {
        return _tasks.map { tasks -> tasks.filter { it.userId == userId } }
    }
    
    override suspend fun addTask(task: Task) {
        val dto = TaskDto(
            id = task.id.ifBlank { UUID.randomUUID().toString() },
            user_id = task.userId ?: "",
            subject_id = task.subjectId,
            title = task.title,
            due_date_time = task.dueDateTime.atOffset(ZoneOffset.UTC).format(dateFormatter),
            is_completed = task.isCompleted
        )
        
        client.postgrest.from("tasks").insert(dto)
        
        // Actualizar estado local
        refreshTasks()
    }
    
    override suspend fun completeTask(taskId: String) {
        client.postgrest
            .from("tasks")
            .update({
                set("is_completed", true)
            }) {
                filter {
                    eq("id", taskId)
                }
            }
        
        refreshTasks()
    }
    
    override suspend fun deleteTask(taskId: String) {
        client.postgrest
            .from("tasks")
            .delete {
                filter {
                    eq("id", taskId)
                }
            }
        
        refreshTasks()
    }
    
    override suspend fun updateTask(task: Task) {
        client.postgrest
            .from("tasks")
            .update({
                set("title", task.title)
                set("subject_id", task.subjectId)
                set("due_date_time", task.dueDateTime.atOffset(ZoneOffset.UTC).format(dateFormatter))
                set("is_completed", task.isCompleted)
            }) {
                filter {
                    eq("id", task.id)
                }
            }
        
        refreshTasks()
    }
    
    /**
     * Carga todas las tareas desde Supabase y actualiza el estado local.
     */
    suspend fun refreshTasks() {
        try {
            val dtos = client.postgrest
                .from("tasks")
                .select()
                .decodeList<TaskDto>()
            
            _tasks.value = dtos.map { dto ->
                Task(
                    id = dto.id,
                    userId = dto.user_id,
                    subjectId = dto.subject_id,
                    title = dto.title,
                    dueDateTime = LocalDateTime.parse(dto.due_date_time, dateFormatter),
                    isCompleted = dto.is_completed
                )
            }
        } catch (e: Exception) {
            // Manejar error silenciosamente o log
        }
    }
    
    /**
     * Carga tareas de un usuario específico.
     */
    suspend fun refreshTasksForUser(userId: String) {
        try {
            val dtos = client.postgrest
                .from("tasks")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<TaskDto>()
            
            _tasks.value = dtos.map { dto ->
                Task(
                    id = dto.id,
                    userId = dto.user_id,
                    subjectId = dto.subject_id,
                    title = dto.title,
                    dueDateTime = LocalDateTime.parse(dto.due_date_time, dateFormatter),
                    isCompleted = dto.is_completed
                )
            }
        } catch (e: Exception) {
            // Manejar error
        }
    }
}
