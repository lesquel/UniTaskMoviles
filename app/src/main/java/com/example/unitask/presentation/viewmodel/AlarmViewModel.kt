package com.example.unitask.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitask.domain.model.NotificationSetting
import com.example.unitask.domain.usecase.CancelAlarmUseCase
import com.example.unitask.domain.usecase.ScheduleAlarmUseCase
import com.example.unitask.domain.usecase.GetAllNotificationsUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val scheduleUseCase: ScheduleAlarmUseCase,
    private val cancelUseCase: CancelAlarmUseCase,
    private val getAllNotificationsUseCase: GetAllNotificationsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<List<NotificationSetting>>(emptyList())
    val uiState: StateFlow<List<NotificationSetting>> = _uiState

    init {
        // Observe repository and update uiState
        viewModelScope.launch {
            getAllNotificationsUseCase().collect { list ->
                _uiState.value = list
            }
        }
    }

    fun schedule(setting: NotificationSetting) {
        viewModelScope.launch {
            scheduleUseCase(setting)
        }
    }

    fun cancel(id: String) {
        viewModelScope.launch {
            cancelUseCase(id)
        }
    }
}
