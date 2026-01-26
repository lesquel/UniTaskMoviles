package com.example.unitask.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitask.domain.model.AlarmTemplate
import com.example.unitask.domain.model.NotificationSetting
import com.example.unitask.domain.model.Subject
import com.example.unitask.domain.model.Task
import com.example.unitask.domain.repository.UserRepository
import com.example.unitask.domain.usecase.AddTaskUseCase
import com.example.unitask.domain.usecase.GetSubjectsUseCase
import com.example.unitask.domain.usecase.GetTaskByIdUseCase
import com.example.unitask.domain.usecase.ScheduleAlarmUseCase
import com.example.unitask.domain.usecase.UpdateTaskUseCase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddTaskViewModel(
    getSubjectsUseCase: GetSubjectsUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val scheduleAlarmUseCase: ScheduleAlarmUseCase,
    private val userRepository: UserRepository,
    private val nowProvider: () -> LocalDateTime,
    initialTaskId: String? = null
) : ViewModel() {
    
    // Plantillas de alarma disponibles (por defecto usa las predefinidas)
    val alarmTemplates: List<AlarmTemplate> = AlarmTemplate.defaults

    private val _uiState = MutableStateFlow(AddTaskUiState())
    val uiState: StateFlow<AddTaskUiState> = _uiState.asStateFlow()

    private val subjectsFlow = getSubjectsUseCase()
        .map { list -> list.map { it.toOption() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _events = MutableSharedFlow<AddTaskEvent>()
    val events = _events
    private var editingTask: Task? = null
    private var currentUserId: String? = null

    companion object {
        const val MAX_TITLE_LENGTH = 50
    }

    init {
        resetDueDateDefaults()
        viewModelScope.launch {
            // Obtener el usuario actual y su racha
            val user = userRepository.getCurrentUser()
            currentUserId = user?.id
            
            val stats = userRepository.getUserStats(user?.id ?: "")
            _uiState.updateDetails { copy(currentStreak = stats?.currentStreak ?: 0) }
            
            subjectsFlow.collect { options ->
                _uiState.updateDetails {
                    val updatedSelection = selectedSubjectId ?: options.firstOrNull()?.id
                    copy(subjects = options, selectedSubjectId = updatedSelection)
                }
            }
        }
        initialTaskId?.let { loadTask(it) }
    }

    fun onTitleChanged(value: String) {
        if (value.length > MAX_TITLE_LENGTH) {
            _uiState.updateDetails { copy(error = AddTaskError.TitleTooLong) }
            return
        }
        _uiState.updateDetails { copy(title = value, error = null) }
    }

    fun onSubjectSelected(subjectId: String) {
        _uiState.updateDetails { copy(selectedSubjectId = subjectId, error = null) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.updateDetails { copy(dueDate = date, error = null) }
    }

    fun onTimeSelected(time: LocalTime) {
        _uiState.updateDetails { copy(dueTime = time, error = null) }
    }

    fun onAlarmTemplateToggled(template: AlarmTemplate) {
        _uiState.updateDetails {
            val currentSelected = selectedAlarmTemplates.toMutableSet()
            if (currentSelected.contains(template.id)) {
                currentSelected.remove(template.id)
            } else {
                currentSelected.add(template.id)
            }
            copy(selectedAlarmTemplates = currentSelected)
        }
    }

    fun clearSelectedAlarms() {
        _uiState.updateDetails { copy(selectedAlarmTemplates = emptySet()) }
    }

    fun submit() {
        val current = _uiState.value
        val subjectId = current.selectedSubjectId
        val date = current.dueDate
        val time = current.dueTime
        val rawTitle = current.title.trim()

        if (rawTitle.isBlank()) {
            _uiState.updateDetails { copy(error = AddTaskError.TitleRequired) }
            return
        }

        if (rawTitle.length > MAX_TITLE_LENGTH) {
            _uiState.updateDetails { copy(error = AddTaskError.TitleTooLong) }
            return
        }

        if (subjectId == null) {
            _uiState.updateDetails { copy(error = AddTaskError.SubjectRequired) }
            return
        }

        if (date == null || time == null) {
            _uiState.updateDetails { copy(error = AddTaskError.DateTimeRequired) }
            return
        }

        val dueDateTime = LocalDateTime.of(date, time)

        val userId = currentUserId
        if (userId.isNullOrEmpty()) {
            _uiState.updateDetails { copy(error = AddTaskError.SubmitError("Debes iniciar sesión para crear tareas")) }
            return
        }

        viewModelScope.launch {
            _uiState.updateDetails { copy(isSubmitting = true, error = null) }
            runCatching {
                if (current.editingTaskId != null && editingTask != null) {
                    val updatedTask = editingTask!!.copy(
                        title = rawTitle,
                        subjectId = subjectId,
                        dueDateTime = dueDateTime
                    )
                    updateTaskUseCase(updatedTask)
                    updatedTask
                } else {
                    addTaskUseCase(
                        userId = userId,
                        title = rawTitle,
                        subjectId = subjectId,
                        dueDateTime = dueDateTime
                    )
                }
            }
                .onSuccess { task ->
                    // Crear alarmas basadas en las plantillas seleccionadas
                    val selectedTemplates = current.selectedAlarmTemplates
                    if (selectedTemplates.isNotEmpty()) {
                        val subjectName = current.subjects.find { it.id == subjectId }?.name ?: ""
                        scheduleAlarmsForTask(
                            taskId = task.id,
                            dueDateTime = dueDateTime,
                            selectedTemplateIds = selectedTemplates,
                            taskTitle = rawTitle,
                            subjectName = subjectName
                        )
                    }
                    
                    val isUpdate = current.editingTaskId != null
                    _events.emit(AddTaskEvent.Success(task.id, isUpdate))
                    editingTask = null
                    _uiState.value = AddTaskUiState(
                        subjects = subjectsFlow.value,
                        selectedSubjectId = subjectsFlow.value.firstOrNull()?.id
                    ).withDefaultDueDate()
                }
                .onFailure { error ->
                    val submitError = AddTaskError.SubmitError(error.message ?: "Error al guardar")
                    _uiState.updateDetails { copy(isSubmitting = false, error = submitError) }
                    _events.emit(AddTaskEvent.Error(submitError))
                }
        }
    }
    
    /**
     * Programa alarmas para la tarea basándose en las plantillas seleccionadas.
     */
    private fun scheduleAlarmsForTask(
        taskId: String,
        dueDateTime: LocalDateTime,
        selectedTemplateIds: Set<String>,
        taskTitle: String,
        subjectName: String
    ) {
        viewModelScope.launch {
            val dueDateMillis = dueDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            alarmTemplates
                .filter { it.id in selectedTemplateIds }
                .forEach { template ->
                    val triggerAtMillis = dueDateMillis - (template.minutesBefore * 60 * 1000L)
                    
                    // Solo programar si la alarma es en el futuro
                    if (triggerAtMillis > System.currentTimeMillis()) {
                        val notificationSetting = NotificationSetting(
                            id = UUID.randomUUID().toString(),
                            taskId = taskId,
                            enabled = true,
                            triggerAtMillis = triggerAtMillis,
                            repeatIntervalMillis = null,
                            useMinutes = template.minutesBefore < 60,
                            exact = true,
                            taskTitle = taskTitle,
                            subjectName = subjectName
                        )
                        scheduleAlarmUseCase(notificationSetting)
                    }
                }
        }
    }

    private fun resetDueDateDefaults() {
        _uiState.value = _uiState.value.withDefaultDueDate()
    }

    private fun AddTaskUiState.withDefaultDueDate(): AddTaskUiState {
        val default = defaultDueDateTime()
        return copy(
            dueDate = default.toLocalDate(),
            dueTime = default.toLocalTime().withSecond(0).withNano(0)
        )
    }

    private fun defaultDueDateTime(): LocalDateTime = nowProvider().plusHours(1)

    private fun Subject.toOption(): SubjectOption = SubjectOption(
        id = id,
        name = name,
        colorHex = colorHex
    )

    private inline fun MutableStateFlow<AddTaskUiState>.updateDetails(
        crossinline transform: AddTaskUiState.() -> AddTaskUiState
    ) {
        value = value.transform()
    }

    private fun loadTask(taskId: String) {
        viewModelScope.launch {
            val task = getTaskByIdUseCase(taskId)
            if (task == null) {
                _uiState.updateDetails { copy(error = AddTaskError.SubmitError("Tarea no encontrada.")) }
                return@launch
            }
            editingTask = task
            _uiState.updateDetails {
                copy(
                    title = task.title,
                    selectedSubjectId = task.subjectId,
                    dueDate = task.dueDateTime.toLocalDate(),
                    dueTime = task.dueDateTime.toLocalTime(),
                    editingTaskId = task.id,
                    error = null
                )
            }
        }
    }
}

data class AddTaskUiState(
    val title: String = "",
    val selectedSubjectId: String? = null,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val subjects: List<SubjectOption> = emptyList(),
    val selectedAlarmTemplates: Set<String> = emptySet(), // IDs de plantillas seleccionadas
    val isSubmitting: Boolean = false,
    val error: AddTaskError? = null,
    val editingTaskId: String? = null,
    val currentStreak: Int = 0
)

/**
 * Sealed interface representing all possible validation/submission errors.
 * Using a sealed hierarchy is safer than raw strings or generic exceptions.
 */
sealed interface AddTaskError {
    data object TitleRequired : AddTaskError
    data object TitleTooLong : AddTaskError
    data object SubjectRequired : AddTaskError
    data object DateTimeRequired : AddTaskError
    data class SubmitError(val message: String) : AddTaskError
}

data class SubjectOption(
    val id: String,
    val name: String,
    val colorHex: String
)

sealed class AddTaskEvent {
    data class Success(val taskId: String, val isUpdate: Boolean) : AddTaskEvent()
    data class Error(val error: AddTaskError) : AddTaskEvent()
}
