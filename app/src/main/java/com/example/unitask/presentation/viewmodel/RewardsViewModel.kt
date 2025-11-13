package com.example.unitask.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitask.domain.usecase.AwardXpUseCase
import com.example.unitask.domain.usecase.GetXpUseCase
import com.example.unitask.domain.usecase.GetLevelUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RewardsViewModel(
    private val awardXpUseCase: AwardXpUseCase,
    private val getXpUseCase: GetXpUseCase,
    private val getLevelUseCase: GetLevelUseCase
) : ViewModel() {
    val xp: StateFlow<Int> = getXpUseCase().stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val level: StateFlow<Int> = getLevelUseCase().stateIn(viewModelScope, SharingStarted.Eagerly, 1)

    fun award(xpAmount: Int) {
        viewModelScope.launch {
            awardXpUseCase(xpAmount)
        }
    }
}
