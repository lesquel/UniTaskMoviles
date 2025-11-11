package com.example.unitask.domain.usecase

import com.example.unitask.domain.repository.TaskRepository

class CompleteTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String) {
        require(taskId.isNotBlank()) { "Task id cannot be blank." }
        taskRepository.completeTask(taskId)
    }
}
