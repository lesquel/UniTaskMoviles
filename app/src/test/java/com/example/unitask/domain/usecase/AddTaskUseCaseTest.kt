package com.example.unitask.domain.usecase

import com.example.unitask.data.repository.InMemorySubjectRepository
import com.example.unitask.data.repository.InMemoryTaskRepository
import com.example.unitask.domain.model.Subject
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class AddTaskUseCaseTest {

    private val now = LocalDateTime.now()
    private val subject = Subject(id = "subject-1", name = "Arquitectura", colorHex = "#FF0000")
    private val subjectRepository = InMemorySubjectRepository(listOf(subject))
    private val taskRepository = InMemoryTaskRepository()
    private val useCase = AddTaskUseCase(taskRepository, subjectRepository, nowProvider = { now })

    @Test
    fun `creates task with trimmed title`() = runTest {
        val task = useCase("  Nuevo Caso  ", subject.id, now.plusHours(3))
        val stored = taskRepository.getTasksFlow().first()
        assertEquals("Nuevo Caso", task.title)
        assertEquals(task.id, stored.single().id)
    }

    @Test
    fun `throws when title is blank`() = runTest {
        try {
            useCase("   ", subject.id, now.plusHours(1))
            fail("Expected IllegalArgumentException")
        } catch (expected: IllegalArgumentException) {
            // success
        }
    }
}
