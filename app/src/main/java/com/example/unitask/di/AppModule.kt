package com.example.unitask.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.unitask.data.repository.InMemorySubjectRepository
import com.example.unitask.data.repository.InMemoryTaskRepository
import com.example.unitask.data.repository.SampleData
import com.example.unitask.domain.repository.SubjectRepository
import com.example.unitask.domain.repository.TaskRepository
import com.example.unitask.domain.usecase.AddSubjectUseCase
import com.example.unitask.domain.usecase.AddTaskUseCase
import com.example.unitask.domain.usecase.CompleteTaskUseCase
import com.example.unitask.domain.usecase.DeleteSubjectUseCase
import com.example.unitask.domain.usecase.EditSubjectUseCase
import com.example.unitask.domain.usecase.GetAllTasksUseCase
import com.example.unitask.domain.usecase.GetSubjectsUseCase
import com.example.unitask.domain.usecase.GetUrgentTasksUseCase
import com.example.unitask.presentation.viewmodel.AddTaskViewModel
import com.example.unitask.presentation.viewmodel.DashboardViewModel
import com.example.unitask.presentation.viewmodel.SubjectsViewModel
import java.time.LocalDateTime

/**
 * Simple manual dependency container. Replace with Hilt/Dagger when ready.
 */
object AppModule {

    // Data sources
    private val subjectRepository: SubjectRepository by lazy {
        InMemorySubjectRepository(SampleData.subjects())
    }
    private val taskRepository: TaskRepository by lazy {
        InMemoryTaskRepository(SampleData.tasks())
    }

    // Use cases
    private val getAllTasksUseCase: GetAllTasksUseCase by lazy {
        GetAllTasksUseCase(taskRepository)
    }
    private val getUrgentTasksUseCase: GetUrgentTasksUseCase by lazy {
        GetUrgentTasksUseCase(taskRepository)
    }
    private val addTaskUseCase: AddTaskUseCase by lazy {
        AddTaskUseCase(taskRepository, subjectRepository)
    }
    private val completeTaskUseCase: CompleteTaskUseCase by lazy {
        CompleteTaskUseCase(taskRepository)
    }
    private val getSubjectsUseCase: GetSubjectsUseCase by lazy {
        GetSubjectsUseCase(subjectRepository)
    }
    private val addSubjectUseCase: AddSubjectUseCase by lazy {
        AddSubjectUseCase(subjectRepository)
    }
    private val editSubjectUseCase: EditSubjectUseCase by lazy {
        EditSubjectUseCase(subjectRepository)
    }
    private val deleteSubjectUseCase: DeleteSubjectUseCase by lazy {
        DeleteSubjectUseCase(subjectRepository, taskRepository)
    }

    val viewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            DashboardViewModel(
                getAllTasksUseCase = getAllTasksUseCase,
                getUrgentTasksUseCase = getUrgentTasksUseCase,
                getSubjectsUseCase = getSubjectsUseCase,
                completeTaskUseCase = completeTaskUseCase
            )
        }
        initializer {
            AddTaskViewModel(
                getSubjectsUseCase = getSubjectsUseCase,
                addTaskUseCase = addTaskUseCase,
                nowProvider = { LocalDateTime.now() }
            )
        }
        initializer {
            SubjectsViewModel(
                getSubjectsUseCase = getSubjectsUseCase,
                addSubjectUseCase = addSubjectUseCase,
                editSubjectUseCase = editSubjectUseCase,
                deleteSubjectUseCase = deleteSubjectUseCase
            )
        }
    }
}
