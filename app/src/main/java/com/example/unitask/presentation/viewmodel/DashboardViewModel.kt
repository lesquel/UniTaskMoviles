package com.example.unitask.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitask.domain.model.NotificationSetting
import com.example.unitask.domain.model.Subject
import com.example.unitask.domain.model.Task
import com.example.unitask.domain.repository.UserRepository
import com.example.unitask.domain.usecase.AwardXpUseCase
import com.example.unitask.domain.usecase.CompleteTaskUseCase
import com.example.unitask.domain.usecase.DeleteTaskUseCase
import com.example.unitask.domain.usecase.GetAllNotificationsUseCase
import com.example.unitask.domain.usecase.GetAllTasksUseCase
import com.example.unitask.domain.usecase.GetSubjectsUseCase
import com.example.unitask.domain.usecase.GetUrgentTasksUseCase
import com.example.unitask.presentation.ui.components.DayFilter
import java.time.DayOfWeek
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
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val awardXpUseCase: AwardXpUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    companion object {
        const val PAGE_SIZE = 10
    }

    private val formatter = DateTimeFormatter.ofPattern("dd MMM HH:mm", Locale.getDefault())

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Estado interno para almacenar todas las tareas sin filtrar
    private var allTasksUnfiltered: List<TaskUiModel> = emptyList()
    
    // ID del usuario actual
    private var currentUserId: String? = null

    init {
        loadCurrentUserAndObserveData()
    }
    
    private fun loadCurrentUserAndObserveData() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            currentUserId = user?.id
            
            // Cargar racha del usuario
            user?.id?.let { userId ->
                val stats = userRepository.getUserStats(userId)
                _uiState.update { it.copy(currentStreak = stats?.currentStreak ?: 0) }
            }
            
            observeDashboardData()
        }
    }

    fun onTaskCompleted(taskId: String) {
        viewModelScope.launch {
            runCatching { completeTaskUseCase(taskId) }
                .onSuccess {
                    // Actualizar estadísticas del usuario en la base de datos
                    currentUserId?.let { userId ->
                        userRepository.incrementTasksCompleted(userId)
                        userRepository.addXp(userId, 25)
                        
                        // Actualizar racha en el UI
                        val stats = userRepository.getUserStats(userId)
                        _uiState.update { it.copy(currentStreak = stats?.currentStreak ?: 0) }
                    }
                    // También actualizar en SharedPreferences para compatibilidad
                    awardXpUseCase(25)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }
    
    /**
     * Elimina una tarea por su ID.
     */
    fun onTaskDeleted(taskId: String) {
        viewModelScope.launch {
            runCatching { deleteTaskUseCase(taskId) }
                .onSuccess {
                    // La UI se actualizará automáticamente por el Flow
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = "Error al eliminar: ${error.message}") }
                }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onDayFilterSelected(filter: DayFilter) {
        _uiState.update { currentState ->
            val filteredTasks = applyDayFilter(allTasksUnfiltered, filter)
            val pagedTasks = filteredTasks.take(PAGE_SIZE)
            currentState.copy(
                selectedDayFilter = filter,
                filteredTasks = filteredTasks,
                displayedTasks = pagedTasks,
                currentPage = 0,
                hasMorePages = filteredTasks.size > PAGE_SIZE
            )
        }
    }

    fun loadNextPage() {
        _uiState.update { currentState ->
            if (!currentState.hasMorePages) return@update currentState
            
            val nextPage = currentState.currentPage + 1
            val endIndex = minOf((nextPage + 1) * PAGE_SIZE, currentState.filteredTasks.size)
            val pagedTasks = currentState.filteredTasks.take(endIndex)
            
            currentState.copy(
                displayedTasks = pagedTasks,
                currentPage = nextPage,
                hasMorePages = endIndex < currentState.filteredTasks.size
            )
        }
    }
    
    /**
     * Refresca los datos del dashboard.
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            
            // Recargar datos del usuario
            val user = userRepository.getCurrentUser()
            currentUserId = user?.id
            
            // Actualizar racha
            user?.id?.let { userId ->
                val stats = userRepository.getUserStats(userId)
                _uiState.update { it.copy(currentStreak = stats?.currentStreak ?: 0) }
            }
            
            // Esperar un poco para mostrar el indicador de refresh
            kotlinx.coroutines.delay(500)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun applyDayFilter(tasks: List<TaskUiModel>, filter: DayFilter): List<TaskUiModel> {
        return when (filter) {
            is DayFilter.All -> tasks
            is DayFilter.SpecificDay -> tasks.filter { task ->
                task.dueDayOfWeek == filter.dayOfWeek
            }
        }
    }

    private fun observeDashboardData() {
        viewModelScope.launch {
            combine(
                getUrgentTasksUseCase(userId = currentUserId),
                getAllTasksUseCase(userId = currentUserId),
                getSubjectsUseCase(),
                getAllNotificationsUseCase()
            ) { urgent, all, subjects, notifications ->
                val grouped = notifications.groupBy { it.taskId }
                val allTasksUi = all.map { it.toUiModel(subjects, grouped[it.id]) }
                allTasksUnfiltered = allTasksUi
                
                val currentFilter = _uiState.value.selectedDayFilter
                val filteredTasks = applyDayFilter(allTasksUi, currentFilter)
                val pagedTasks = filteredTasks.take(PAGE_SIZE)
                
                _uiState.value.copy(
                    urgentTasks = urgent.map { it.toUiModel(subjects, grouped[it.id]) },
                    allTasks = allTasksUi,
                    filteredTasks = filteredTasks,
                    displayedTasks = pagedTasks,
                    selectedDayFilter = currentFilter,
                    currentPage = 0,
                    hasMorePages = filteredTasks.size > PAGE_SIZE,
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
            dueDayOfWeek = dueDateTime.dayOfWeek,
            nextAlarmAtMillis = nextAlarmAt,
            alarmCount = alarmCount,
            isCompleted = isCompleted
        )
    }
}

data class DashboardUiState(
    val urgentTasks: List<TaskUiModel> = emptyList(),
    val allTasks: List<TaskUiModel> = emptyList(),
    val filteredTasks: List<TaskUiModel> = emptyList(),
    val displayedTasks: List<TaskUiModel> = emptyList(),
    val selectedDayFilter: DayFilter = DayFilter.All,
    val currentPage: Int = 0,
    val hasMorePages: Boolean = false,
    val currentStreak: Int = 0,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

data class TaskUiModel(
    val id: String,
    val title: String,
    val subjectName: String,
    val subjectColorHex: String,
    val dueFormatted: String,
    val dueDayOfWeek: DayOfWeek? = null,
    val isCompleted: Boolean,
    val nextAlarmAtMillis: Long? = null,
    val alarmCount: Int = 0
)
