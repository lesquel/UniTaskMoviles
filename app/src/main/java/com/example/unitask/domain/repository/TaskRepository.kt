package com.example.unitask.domain.repository

import com.example.unitask.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasksFlow(): Flow<List<Task>>
    suspend fun addTask(task: Task)
    suspend fun completeTask(taskId: String)
    suspend fun deleteTask(taskId: String)
    suspend fun updateTask(task: Task)
}
