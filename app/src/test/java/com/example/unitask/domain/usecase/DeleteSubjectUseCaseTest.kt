package com.example.unitask.domain.usecase

import com.example.unitask.data.repository.InMemorySubjectRepository
import com.example.unitask.data.repository.InMemoryTaskRepository
import com.example.unitask.domain.model.Subject
import com.example.unitask.domain.model.Task
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class DeleteSubjectUseCaseTest {

    private val now = LocalDateTime.of(2025, 1, 2, 9, 0)
    private val subject = Subject(id = "subject-1", name = "SO", colorHex = "#00FF00")
    private val subjectsRepo = InMemorySubjectRepository(listOf(subject))
    private val tasksRepo = InMemoryTaskRepository(
        listOf(
            Task(
                id = "task-1",
                title = "Informe",
                subjectId = subject.id,
                dueDateTime = now.plusDays(1)
            )
        )
    )
    private val useCase = DeleteSubjectUseCase(subjectsRepo, tasksRepo)

    @Test
    fun `deletes subject and related tasks when cascade`() = runTest {
        useCase(subjectId = subject.id, cascade = true)
        val remainingSubjects = subjectsRepo.getSubjectsFlow().first()
        val remainingTasks = tasksRepo.getTasksFlow().first()
        assertEquals(0, remainingSubjects.size)
        assertEquals(0, remainingTasks.size)
    }

    @Test
    fun `throws when cascade disabled and tasks exist`() = runTest {
        try {
            useCase(subjectId = subject.id, cascade = false)
            fail("Expected IllegalStateException")
        } catch (expected: IllegalStateException) {
            // success
        }
    }
}
