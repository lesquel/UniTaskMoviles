package com.example.unitask.domain.usecase

import com.example.unitask.data.repository.InMemoryTaskRepository
import com.example.unitask.domain.model.Task
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetAllTasksUseCaseTest {

    private val now = LocalDateTime.of(2025, 1, 1, 10, 0)
    private val tasks = listOf(
        Task(
            id = "task-1",
            title = "Ensayo",
            subjectId = "subject-1",
            dueDateTime = now.plusHours(10)
        ),
        Task(
            id = "task-2",
            title = "Entrega",
            subjectId = "subject-1",
            dueDateTime = now.plusHours(12),
            isCompleted = true
        )
    )
    private val repository = InMemoryTaskRepository(tasks)
    private val useCase = GetAllTasksUseCase(repository)

    @Test
    fun `exclude completed tasks by default`() = runTest {
        val result = useCase().first()
        assertEquals(1, result.size)
        assertTrue(result.none { it.isCompleted })
    }

    @Test
    fun `include completed tasks when requested`() = runTest {
        val result = useCase(includeCompleted = true).first()
        assertEquals(2, result.size)
    }
}