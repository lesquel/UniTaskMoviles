package com.example.unitask.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the 'tasks' table.
 * Defines standard CRUD operations using Coroutines and Flow.
 */
@Dao
interface TaskDao {
    /**
     * Returns a reactive stream of all tasks, ordered by due date.
     * The Flow emits a new list whenever the table is updated.
     */
    @Query("SELECT * FROM tasks ORDER BY dueDateTime ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    /**
     * Inserts or Updates a task. If ID exists, it replaces the row.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?
}
