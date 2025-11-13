package com.example.unitask.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitask.domain.usecase.AwardXpUseCase
import com.example.unitask.domain.usecase.GetXpUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RewardsViewModel(
    private val awardXpUseCase: AwardXpUseCase,
    private val getXpUseCase: GetXpUseCase
) : ViewModel() {
    val xp: StateFlow<Int> = getXpUseCase().stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun award(xpAmount: Int) {
        viewModelScope.launch {
            awardXpUseCase(xpAmount)
        }
    }
}
