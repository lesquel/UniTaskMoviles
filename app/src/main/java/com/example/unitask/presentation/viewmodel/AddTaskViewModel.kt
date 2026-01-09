package com.example.unitask.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitask.domain.model.Subject
import com.example.unitask.domain.model.Task
import com.example.unitask.domain.usecase.AddTaskUseCase
import com.example.unitask.domain.usecase.GetSubjectsUseCase
import com.example.unitask.domain.usecase.GetTaskByIdUseCase
import com.example.unitask.domain.usecase.UpdateTaskUseCase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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
    private val nowProvider: () -> LocalDateTime,
    initialTaskId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTaskUiState())
    val uiState: StateFlow<AddTaskUiState> = _uiState.asStateFlow()

    private val subjectsFlow = getSubjectsUseCase()
        .map { list -> list.map { it.toOption() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _events = MutableSharedFlow<AddTaskEvent>()
    val events = _events
    private var editingTask: Task? = null

    companion object {
        const val MAX_TITLE_LENGTH = 50
    }

    init {
        resetDueDateDefaults()
        viewModelScope.launch {
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
            _uiState.updateDetails { copy(errorMessage = "El título no puede exceder $MAX_TITLE_LENGTH caracteres.") }
            return
        }
        _uiState.updateDetails { copy(title = value, errorMessage = null) }
    }

    fun onSubjectSelected(subjectId: String) {
        _uiState.updateDetails { copy(selectedSubjectId = subjectId, errorMessage = null) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.updateDetails { copy(dueDate = date, errorMessage = null) }
    }

    fun onTimeSelected(time: LocalTime) {
        _uiState.updateDetails { copy(dueTime = time, errorMessage = null) }
    }

    fun submit() {
        val current = _uiState.value
        val subjectId = current.selectedSubjectId
        val date = current.dueDate
        val time = current.dueTime
        val rawTitle = current.title.trim()

        if (rawTitle.isBlank()) {
            _uiState.updateDetails { copy(errorMessage = "El título es requerido.") }
            return
        }

        if (rawTitle.length > MAX_TITLE_LENGTH) {
            _uiState.updateDetails { copy(errorMessage = "El título es demasiado largo.") }
            return
        }

        if (subjectId == null) {
            _uiState.updateDetails { copy(errorMessage = "Debes seleccionar una asignatura.") }
            return
        }

        if (date == null || time == null) {
            _uiState.updateDetails { copy(errorMessage = "Fecha y hora son requeridas.") }
            return
        }

        val dueDateTime = LocalDateTime.of(date, time)

        viewModelScope.launch {
            _uiState.updateDetails { copy(isSubmitting = true, errorMessage = null) }
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
                        title = rawTitle,
                        subjectId = subjectId,
                        dueDateTime = dueDateTime
                    )
                }
            }
                .onSuccess { task ->
                    val isUpdate = current.editingTaskId != null
                    _events.emit(AddTaskEvent.Success(task.id, isUpdate))
                    editingTask = null
                    _uiState.value = AddTaskUiState(
                        subjects = subjectsFlow.value,
                        selectedSubjectId = subjectsFlow.value.firstOrNull()?.id
                    ).withDefaultDueDate()
                }
                .onFailure { error ->
                    _uiState.updateDetails { copy(isSubmitting = false, errorMessage = error.message) }
                    _events.emit(AddTaskEvent.Error(error.message ?: "Error al guardar"))
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
                _uiState.updateDetails { copy(errorMessage = "Tarea no encontrada.") }
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
                    errorMessage = null
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
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val editingTaskId: String? = null
)

data class SubjectOption(
    val id: String,
    val name: String,
    val colorHex: String
)

sealed class AddTaskEvent {
    data class Success(val taskId: String, val isUpdate: Boolean) : AddTaskEvent()
    data class Error(val message: String) : AddTaskEvent()
}
