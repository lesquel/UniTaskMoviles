package com.example.unitask.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitask.domain.model.NotificationSetting
import com.example.unitask.domain.usecase.CancelAlarmUseCase
import com.example.unitask.domain.usecase.ScheduleAlarmUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val scheduleUseCase: ScheduleAlarmUseCase,
    private val cancelUseCase: CancelAlarmUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<List<NotificationSetting>>(emptyList())
    val uiState: StateFlow<List<NotificationSetting>> = _uiState

    fun schedule(setting: NotificationSetting) {
        viewModelScope.launch {
            scheduleUseCase(setting)
            // TODO: refresh uiState from repository
        }
    }

    fun cancel(id: String) {
        viewModelScope.launch {
            cancelUseCase(id)
            // TODO: refresh uiState from repository
        }
    }
}
