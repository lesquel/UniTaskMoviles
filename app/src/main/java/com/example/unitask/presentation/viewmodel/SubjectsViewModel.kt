package com.example.unitask.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitask.domain.model.Subject
import com.example.unitask.domain.usecase.AddSubjectUseCase
import com.example.unitask.domain.usecase.DeleteSubjectUseCase
import com.example.unitask.domain.usecase.EditSubjectUseCase
import com.example.unitask.domain.usecase.GetSubjectsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SubjectsViewModel(
    getSubjectsUseCase: GetSubjectsUseCase,
    private val addSubjectUseCase: AddSubjectUseCase,
    private val editSubjectUseCase: EditSubjectUseCase,
    private val deleteSubjectUseCase: DeleteSubjectUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubjectsUiState())
    val uiState: StateFlow<SubjectsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SubjectsEvent>()
    val events = _events

    companion object {
        const val MAX_NAME_LENGTH = 30
        const val MAX_TEACHER_LENGTH = 40
        private val HEX_COLOR_REGEX = Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
    }

    init {
        viewModelScope.launch {
            getSubjectsUseCase()
                .catch { error ->
                    _uiState.value = SubjectsUiState(errorMessage = error.message)
                }
                .collect { subjects ->
                    _uiState.value = SubjectsUiState(
                        subjects = subjects.map { it.toItem() }
                    )
                }
        }
    }

    fun addSubject(name: String, colorHex: String, teacher: String?) {
        val validationError = validateSubjectInput(name, colorHex, teacher)
        if (validationError != null) {
            viewModelScope.launch { emitError(validationError) }
            return
        }

        viewModelScope.launch {
            runCatching { addSubjectUseCase(name.trim(), colorHex.trim(), teacher?.trim()) }
                .onFailure { error -> emitError(error.message) }
        }
    }

    fun editSubject(subjectId: String, name: String, colorHex: String, teacher: String?) {
        val validationError = validateSubjectInput(name, colorHex, teacher)
        if (validationError != null) {
            viewModelScope.launch { emitError(validationError) }
            return
        }

        viewModelScope.launch {
            runCatching { editSubjectUseCase(subjectId, name.trim(), colorHex.trim(), teacher?.trim()) }
                .onFailure { error -> emitError(error.message) }
        }
    }

    private fun validateSubjectInput(name: String, colorHex: String, teacher: String?): String? {
        if (name.isBlank()) return "El nombre es requerido."
        if (name.length > MAX_NAME_LENGTH) return "El nombre no puede exceder $MAX_NAME_LENGTH caracteres."
        
        if (colorHex.isBlank()) return "El color es requerido."
        if (!colorHex.matches(HEX_COLOR_REGEX)) return "Formato de color inválido (ej. #FF0000)."
        
        if (teacher != null && teacher.length > MAX_TEACHER_LENGTH) {
            return "El nombre del profesor es demasiado largo."
        }
        return null
    }

    fun deleteSubject(subjectId: String, cascade: Boolean = true) {
        viewModelScope.launch {
            runCatching { deleteSubjectUseCase(subjectId, cascade) }
                .onFailure { error -> emitError(error.message) }
        }
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private suspend fun emitError(message: String?) {
        val text = message ?: "Ocurrió un error"
        _events.emit(SubjectsEvent.Error(text))
        _uiState.value = _uiState.value.copy(errorMessage = text)
    }

    private fun Subject.toItem(): SubjectItem = SubjectItem(
        id = id,
        name = name,
        colorHex = colorHex,
        teacher = teacher
    )
}

data class SubjectsUiState(
    val subjects: List<SubjectItem> = emptyList(),
    val errorMessage: String? = null
)

data class SubjectItem(
    val id: String,
    val name: String,
    val colorHex: String,
    val teacher: String?
)

sealed class SubjectsEvent {
    data class Error(val message: String) : SubjectsEvent()
}
