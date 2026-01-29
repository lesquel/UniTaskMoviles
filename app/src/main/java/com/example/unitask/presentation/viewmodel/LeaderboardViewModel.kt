package com.example.unitask.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitask.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserRankingItem(
    val userId: String,
    val username: String,
    val profileImagePath: String?,
    val totalXp: Int,
    val level: Int,
    val tasksCompleted: Int
)

data class LeaderboardFilter(
    val id: String,
    val displayName: String,
    val subjectId: String? = null // null = todos
)

data class LeaderboardUiState(
    val rankings: List<UserRankingItem> = emptyList(),
    val filters: List<LeaderboardFilter> = listOf(
        LeaderboardFilter("all", "Todos")
    ),
    val selectedFilter: LeaderboardFilter = LeaderboardFilter("all", "Todos"),
    val currentUserId: String? = null,
    val currentUserPosition: Int? = null,
    val currentUserData: UserRankingItem? = null,
    val currentStreak: Int = 0,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false
)

class LeaderboardViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        loadFilters()
        loadLeaderboard()
    }

    private fun loadFilters() {
        viewModelScope.launch {
            try {
                val subjects = userRepository.getSubjectsForFilters()
                val filters = mutableListOf(LeaderboardFilter("all", "Todos"))
                filters.addAll(subjects.map { 
                    LeaderboardFilter(it.id, it.name, it.id) 
                })
                _uiState.update { it.copy(filters = filters) }
            } catch (e: Exception) {
                // Mantener filtro por defecto
            }
        }
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val currentUser = userRepository.getCurrentUser()
                val stats = userRepository.getUserStats(currentUser?.id ?: "")
                val rankings = userRepository.getLeaderboard(
                    subjectId = _uiState.value.selectedFilter.subjectId,
                    limit = 10
                )
                
                val currentUserPosition = if (currentUser != null) {
                    rankings.indexOfFirst { it.userId == currentUser.id }
                        .let { if (it >= 0) it + 1 else null }
                } else null
                
                val currentUserData = rankings.find { it.userId == currentUser?.id }
                
                _uiState.update {
                    it.copy(
                        rankings = rankings,
                        currentUserId = currentUser?.id,
                        currentUserPosition = currentUserPosition,
                        currentUserData = currentUserData,
                        currentStreak = stats?.currentStreak ?: 0,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Refresca los datos del leaderboard.
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            
            try {
                val currentUser = userRepository.getCurrentUser()
                val stats = userRepository.getUserStats(currentUser?.id ?: "")
                val rankings = userRepository.getLeaderboard(
                    subjectId = _uiState.value.selectedFilter.subjectId,
                    limit = 10
                )
                
                val currentUserPosition = if (currentUser != null) {
                    rankings.indexOfFirst { it.userId == currentUser.id }
                        .let { if (it >= 0) it + 1 else null }
                } else null
                
                val currentUserData = rankings.find { it.userId == currentUser?.id }
                
                kotlinx.coroutines.delay(300)
                
                _uiState.update {
                    it.copy(
                        rankings = rankings,
                        currentUserId = currentUser?.id,
                        currentUserPosition = currentUserPosition,
                        currentUserData = currentUserData,
                        currentStreak = stats?.currentStreak ?: 0,
                        isRefreshing = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun onFilterSelected(filter: LeaderboardFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        loadLeaderboard()
    }
}
