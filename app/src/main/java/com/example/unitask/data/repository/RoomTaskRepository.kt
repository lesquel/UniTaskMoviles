package com.example.unitask.data.repository

import com.example.unitask.data.room.TaskDao
import com.example.unitask.data.room.toDomain
import com.example.unitask.data.room.toEntity
import com.example.unitask.domain.model.Task
import com.example.unitask.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * [TaskRepository] implementation using Room as the persistent data source.
 *
 * This class acts as a bridge between the data layer (Room Entities) and the domain layer (Task Models).
 * It is responsible for mapping [TaskEntity] to [Task] and vice versa.
 *
 * @property taskDao The Data Access Object for Task database operations.
 */
class RoomTaskRepository(private val taskDao: TaskDao) : TaskRepository {

    /**
     * Retrieves a stream of all tasks from the database, mapped to domain models.
     * Uses [Flow] to emit updates whenever the database table changes.
     */
    override fun getTasksFlow(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Inserts or updates a task in the database.
     *
     * @param task The domain model to persist.
     */
    override suspend fun addTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }

    /**
     * Marks a task as completed by updating its status in the database.
     *
     * @param taskId The persistent identifier of the task.
     */
    override suspend fun completeTask(taskId: String) {
        val entity = taskDao.getTaskById(taskId)
        entity?.let {
            val completedTask = it.copy(isCompleted = true)
            taskDao.insertTask(completedTask)
        }
    }

    /**
     * Permanently deletes a task from the database.
     *
     * @param taskId The persistent identifier of the task to remove.
     */
    override suspend fun deleteTask(taskId: String) {
        val entity = taskDao.getTaskById(taskId)
        entity?.let {
            taskDao.deleteTask(it)
        }
    }

    /**
     * Updates an existing task's details.
     * In Room, this uses the same [Insert] strategy with REPLACE.
     */
    override suspend fun updateTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }
}
