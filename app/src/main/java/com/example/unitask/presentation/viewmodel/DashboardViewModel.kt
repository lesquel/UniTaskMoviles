package com.example.unitask.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitask.domain.model.NotificationSetting
import com.example.unitask.domain.model.Subject
import com.example.unitask.domain.model.Task
import com.example.unitask.domain.usecase.AwardXpUseCase
import com.example.unitask.domain.usecase.CompleteTaskUseCase
import com.example.unitask.domain.usecase.GetAllNotificationsUseCase
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
    private val getAllNotificationsUseCase: GetAllNotificationsUseCase,
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
                getSubjectsUseCase(),
                getAllNotificationsUseCase()
            ) { urgent, all, subjects, notifications ->
                val grouped = notifications.groupBy { it.taskId }
                DashboardUiState(
                    urgentTasks = urgent.map { it.toUiModel(subjects, grouped[it.id]) },
                    allTasks = all.map { it.toUiModel(subjects, grouped[it.id]) },
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

    private fun Task.toUiModel(subjects: List<Subject>, notifications: List<NotificationSetting>?): TaskUiModel {
        val subject = subjects.find { it.id == subjectId }
        val subjectName = subject?.name ?: "Sin asignatura"
        val subjectColor = subject?.colorHex ?: "#757575"
        val sortedNotifications = notifications.orEmpty()
            .filter { it.enabled }
            .sortedBy { it.triggerAtMillis }
        val nextAlarmAt = sortedNotifications.firstOrNull()?.triggerAtMillis
        val alarmCount = sortedNotifications.size
        return TaskUiModel(
            id = id,
            title = title,
            subjectName = subjectName,
            subjectColorHex = subjectColor,
            dueFormatted = formatter.format(dueDateTime),
            nextAlarmAtMillis = nextAlarmAt,
            alarmCount = alarmCount,
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
    val isCompleted: Boolean,
    val nextAlarmAtMillis: Long? = null,
    val alarmCount: Int = 0
)
