package com.example.unitask.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitask.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class ProfileUiState(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val profileImagePath: String? = null,
    val totalXp: Int = 0,
    val level: Int = 1,
    val tasksCompleted: Int = 0,
    val currentStreak: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }
    
    /**
     * Calcula el nivel basándose en el XP.
     */
    private fun calculateLevel(xp: Int): Int {
        var level = 1
        var requiredXp = 100
        var totalRequired = requiredXp
        
        while (xp >= totalRequired) {
            level++
            requiredXp = level * 100
            totalRequired += requiredXp
        }
        
        return level
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val user = userRepository.getCurrentUser()
                val stats = userRepository.getUserStats(user?.id ?: "")
                
                // Leer XP directamente desde Room (tabla users)
                val xp = user?.totalXp ?: 0
                val level = calculateLevel(xp)
                
                _uiState.update {
                    it.copy(
                        userId = user?.id ?: "",
                        username = user?.username ?: "Usuario",
                        email = user?.email ?: "",
                        profileImagePath = user?.profileImagePath,
                        totalXp = xp,
                        level = level,
                        tasksCompleted = stats?.totalTasksCompleted ?: 0,
                        currentStreak = stats?.currentStreak ?: 0,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    /**
     * Refresca los datos del perfil.
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            
            try {
                val user = userRepository.getCurrentUser()
                val stats = userRepository.getUserStats(user?.id ?: "")
                val xp = user?.totalXp ?: 0
                val level = calculateLevel(xp)
                
                kotlinx.coroutines.delay(300)
                
                _uiState.update {
                    it.copy(
                        userId = user?.id ?: "",
                        username = user?.username ?: "Usuario",
                        email = user?.email ?: "",
                        profileImagePath = user?.profileImagePath,
                        totalXp = xp,
                        level = level,
                        tasksCompleted = stats?.totalTasksCompleted ?: 0,
                        currentStreak = stats?.currentStreak ?: 0,
                        isRefreshing = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRefreshing = false, error = e.message) }
            }
        }
    }

    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onImageSelected(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val userId = _uiState.value.userId.ifEmpty { "default" }
                val imagePath = saveProfileImage(context, uri, userId)
                _uiState.update { it.copy(profileImagePath = imagePath) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar imagen") }
            }
        }
    }

    private fun saveProfileImage(context: Context, uri: Uri, userId: String): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "profile_$userId.jpg")
        file.outputStream().use { output ->
            inputStream?.copyTo(output)
        }
        inputStream?.close()
        return file.absolutePath
    }

    suspend fun saveProfile(): Boolean {
        return try {
            val state = _uiState.value
            userRepository.updateProfile(
                userId = state.userId,
                username = state.username,
                email = state.email,
                profileImagePath = state.profileImagePath
            )
            true
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
            false
        }
    }
}
