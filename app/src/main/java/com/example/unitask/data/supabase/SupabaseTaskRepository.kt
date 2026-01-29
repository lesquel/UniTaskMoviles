package com.example.unitask.data.supabase

import android.util.Log
import com.example.unitask.domain.model.Task
import com.example.unitask.domain.repository.TaskRepository
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Implementación de TaskRepository usando Supabase Postgrest.
 */
class SupabaseTaskRepository : TaskRepository {
    
    private val client = SupabaseClientProvider.client
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Cache local de tareas
    private val _tasks = MutableStateFlow<List<TaskDto>>(emptyList())
    
    companion object {
        private const val TAG = "SupabaseTaskRepo"
        private const val TABLE_NAME = "tasks"
    }
    
    init {
        // Cargar tareas iniciales
        scope.launch {
            refreshAllTasks()
        }
    }
    
    @Serializable
    data class TaskDto(
        val id: String,
        @SerialName("user_id")
        val userId: String,
        @SerialName("subject_id")
        val subjectId: String,
        val title: String,
        @SerialName("due_date_time")
        val dueDateTime: String,
        @SerialName("is_completed")
        val isCompleted: Boolean = false,
        @SerialName("created_at")
        val createdAt: String? = null
    )
    
    override fun getTasksFlow(): Flow<List<Task>> {
        return _tasks.map { dtos -> dtos.map { it.toDomain() } }
    }
    
    override fun getTasksFlowByUserId(userId: String): Flow<List<Task>> {
        return _tasks.map { dtos -> 
            dtos.filter { it.userId == userId }.map { it.toDomain() } 
        }
    }
    
    override suspend fun addTask(task: Task) {
        try {
            Log.d(TAG, "Adding task: ${task.title}")
            val dto = task.toDto()
            
            client.postgrest
                .from(TABLE_NAME)
                .insert(dto)
            
            Log.d(TAG, "Task added successfully")
            refreshAllTasks()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding task", e)
            throw e
        }
    }
    
    override suspend fun completeTask(taskId: String) {
        try {
            Log.d(TAG, "Completing task: $taskId")
            
            client.postgrest
                .from(TABLE_NAME)
                .update({
                    set("is_completed", true)
                }) {
                    filter {
                        eq("id", taskId)
                    }
                }
            
            Log.d(TAG, "Task completed successfully")
            refreshAllTasks()
        } catch (e: Exception) {
            Log.e(TAG, "Error completing task", e)
            throw e
        }
    }
    
    override suspend fun deleteTask(taskId: String) {
        try {
            Log.d(TAG, "Deleting task: $taskId")
            
            client.postgrest
                .from(TABLE_NAME)
                .delete {
                    filter {
                        eq("id", taskId)
                    }
                }
            
            Log.d(TAG, "Task deleted successfully")
            refreshAllTasks()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting task", e)
            throw e
        }
    }
    
    override suspend fun updateTask(task: Task) {
        try {
            Log.d(TAG, "Updating task: ${task.id}")
            
            client.postgrest
                .from(TABLE_NAME)
                .update({
                    set("title", task.title)
                    set("subject_id", task.subjectId)
                    set("due_date_time", task.dueDateTime.toIsoString())
                    set("is_completed", task.isCompleted)
                }) {
                    filter {
                        eq("id", task.id)
                    }
                }
            
            Log.d(TAG, "Task updated successfully")
            refreshAllTasks()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating task", e)
            throw e
        }
    }
    
    /**
     * Refresca todas las tareas desde Supabase.
     */
    suspend fun refreshAllTasks() {
        try {
            Log.d(TAG, "Refreshing all tasks...")
            
            val result = client.postgrest
                .from(TABLE_NAME)
                .select()
                .decodeList<TaskDto>()
            
            _tasks.value = result
            Log.d(TAG, "Loaded ${result.size} tasks")
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing tasks", e)
        }
    }
    
    /**
     * Refresca tareas para un usuario específico.
     */
    suspend fun refreshTasksForUser(userId: String) {
        try {
            Log.d(TAG, "Refreshing tasks for user: $userId")
            
            val result = client.postgrest
                .from(TABLE_NAME)
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<TaskDto>()
            
            // Actualizar solo las tareas de este usuario
            val otherTasks = _tasks.value.filter { it.userId != userId }
            _tasks.value = otherTasks + result
            
            Log.d(TAG, "Loaded ${result.size} tasks for user")
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing tasks for user", e)
        }
    }
    
    // Extensiones para conversión
    private fun TaskDto.toDomain(): Task {
        return Task(
            id = id,
            userId = userId,
            title = title,
            subjectId = subjectId,
            dueDateTime = parseDateTime(dueDateTime),
            createdAt = createdAt?.let { parseDateTime(it) } ?: LocalDateTime.now(),
            isCompleted = isCompleted
        )
    }
    
    private fun Task.toDto(): TaskDto {
        return TaskDto(
            id = id,
            userId = userId,
            subjectId = subjectId,
            title = title,
            dueDateTime = dueDateTime.toIsoString(),
            isCompleted = isCompleted,
            createdAt = createdAt.toIsoString()
        )
    }
    
    private fun parseDateTime(isoString: String): LocalDateTime {
        return try {
            ZonedDateTime.parse(isoString).toLocalDateTime()
        } catch (e: Exception) {
            try {
                LocalDateTime.parse(isoString.replace("Z", "").substringBefore("+"))
            } catch (e2: Exception) {
                LocalDateTime.now()
            }
        }
    }
    
    private fun LocalDateTime.toIsoString(): String {
        return this.atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}
