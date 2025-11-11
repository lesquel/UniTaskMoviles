package com.example.unitask.domain.usecase

import com.example.unitask.data.repository.InMemoryTaskRepository
import com.example.unitask.domain.model.Task
import java.time.Duration
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetUrgentTasksUseCaseTest {

    private val now = LocalDateTime.of(2025, 1, 1, 8, 0)
    private val tasks = listOf(
        Task(
            id = "task-inside",
            title = "Parcial",
            subjectId = "subject-1",
            dueDateTime = now.plusHours(12)
        ),
        Task(
            id = "task-outside",
            title = "Proyecto",
            subjectId = "subject-1",
            dueDateTime = now.plusHours(60)
        )
    )

    private val repository = InMemoryTaskRepository(tasks)
    private val useCase = GetUrgentTasksUseCase(taskRepository = repository, nowProvider = { now })

    @Test
    fun `emits only tasks inside window`() = runTest {
        val result = useCase(window = Duration.ofHours(48)).first()
        assertEquals(listOf("task-inside"), result.map { it.id })
    }
}
