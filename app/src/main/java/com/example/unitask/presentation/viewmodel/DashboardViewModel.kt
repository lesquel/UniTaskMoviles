package com.example.unitask.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitask.domain.model.Subject
import com.example.unitask.domain.model.Task
import com.example.unitask.domain.usecase.AwardXpUseCase
import com.example.unitask.domain.usecase.CompleteTaskUseCase
import com.example.unitask.domain.usecase.GetAllTasksUseCase
import com.example.unitask.domain.usecase.GetSubjectsUseCase
import com.example.unitask.domain.usecase.GetUrgentTasksUseCase
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val getUrgentTasksUseCase: GetUrgentTasksUseCase,
    private val getSubjectsUseCase: GetSubjectsUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val awardXpUseCase: AwardXpUseCase
) : ViewModel() {

    private val formatter = DateTimeFormatter.ofPattern("dd MMM HH:mm", Locale.getDefault())

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeDashboardData()
    }

    fun onTaskCompleted(taskId: String) {
        viewModelScope.launch {
            runCatching { completeTaskUseCase(taskId) }
                .onSuccess {
                    awardXpUseCase(25)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun observeDashboardData() {
        viewModelScope.launch {
            combine(
                getUrgentTasksUseCase(),
                getAllTasksUseCase(),
                getSubjectsUseCase()
            ) { urgent, all, subjects ->
                DashboardUiState(
                    urgentTasks = urgent.map { it.toUiModel(subjects) },
                    allTasks = all.map { it.toUiModel(subjects) },
                    isLoading = false,
                    errorMessage = null
                )
            }
                .catch { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    private fun Task.toUiModel(subjects: List<Subject>): TaskUiModel {
        val subject = subjects.find { it.id == subjectId }
        val subjectName = subject?.name ?: "Sin asignatura"
        val subjectColor = subject?.colorHex ?: "#757575"
        return TaskUiModel(
            id = id,
            title = title,
            subjectName = subjectName,
            subjectColorHex = subjectColor,
            dueFormatted = formatter.format(dueDateTime),
            isCompleted = isCompleted
        )
    }
}

data class DashboardUiState(
    val urgentTasks: List<TaskUiModel> = emptyList(),
    val allTasks: List<TaskUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

data class TaskUiModel(
    val id: String,
    val title: String,
    val subjectName: String,
    val subjectColorHex: String,
    val dueFormatted: String,
    val isCompleted: Boolean
)
