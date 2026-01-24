package com.example.unitask.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitask.domain.repository.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val username: String = "",
    val email: String = "",
    val usernameOrEmail: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false
)

sealed class AuthEvent {
    object LoginSuccess : AuthEvent()
    object RegisterSuccess : AuthEvent()
    data class Error(val message: String) : AuthEvent()
}

class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(username = value) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value) }
    }

    fun onUsernameOrEmailChanged(value: String) {
        _uiState.update { it.copy(usernameOrEmail = value) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmPassword = value) }
    }

    fun login() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val state = _uiState.value
            val result = userRepository.login(state.usernameOrEmail, state.password)
            
            result.fold(
                onSuccess = {
                    _events.send(AuthEvent.LoginSuccess)
                },
                onFailure = { error ->
                    _events.send(AuthEvent.Error(error.message ?: "Error al iniciar sesión"))
                }
            )
            
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun register() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val state = _uiState.value
            
            // Validaciones
            if (state.password.length < 6) {
                _events.send(AuthEvent.Error("La contraseña debe tener al menos 6 caracteres"))
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            
            if (state.password != state.confirmPassword) {
                _events.send(AuthEvent.Error("Las contraseñas no coinciden"))
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            
            val result = userRepository.register(state.username, state.email, state.password)
            
            result.fold(
                onSuccess = {
                    _events.send(AuthEvent.RegisterSuccess)
                },
                onFailure = { error ->
                    _events.send(AuthEvent.Error(error.message ?: "Error al registrar"))
                }
            )
            
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
