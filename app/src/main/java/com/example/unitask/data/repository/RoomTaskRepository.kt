package com.example.unitask.data.repository

import com.example.unitask.data.room.TaskDao
import com.example.unitask.data.room.toDomain
import com.example.unitask.data.room.toEntity
import com.example.unitask.domain.model.Task
import com.example.unitask.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTaskRepository(private val taskDao: TaskDao) : TaskRepository {

    override fun getTasksFlow(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }

    override suspend fun completeTask(taskId: String) {
        val entity = taskDao.getTaskById(taskId)
        entity?.let {
            val completedTask = it.copy(isCompleted = true)
            taskDao.insertTask(completedTask)
        }
    }

    override suspend fun deleteTask(taskId: String) {
        val entity = taskDao.getTaskById(taskId)
        entity?.let {
            taskDao.deleteTask(it)
        }
    }

    override suspend fun updateTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }
}
