package com.example.unitask.domain.usecase

import com.example.unitask.domain.model.Task
import com.example.unitask.domain.repository.TaskRepository
import java.time.Duration
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetUrgentTasksUseCase(
    private val taskRepository: TaskRepository,
    private val nowProvider: () -> LocalDateTime = { LocalDateTime.now() }
) {
    operator fun invoke(
        window: Duration = Duration.ofHours(48),
        includeCompleted: Boolean = false
    ): Flow<List<Task>> {
        require(!window.isNegative && !window.isZero) { "Window duration must be positive." }
        return taskRepository.getTasksFlow().map { tasks ->
            val now = nowProvider()
            val windowEnd = now.plus(window)
            tasks.filter { task ->
                val due = task.dueDateTime
                val insideWindow = !due.isBefore(now) && (due.isBefore(windowEnd) || due.isEqual(windowEnd))
                insideWindow && (includeCompleted || !task.isCompleted)
            }
        }
    }
}
