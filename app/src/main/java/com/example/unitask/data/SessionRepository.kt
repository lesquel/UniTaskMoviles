package com.example.unitask.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repositorio para gestionar el estado de sesión del usuario.
 * Persiste información sobre si el usuario está logueado y si ya vio el onboarding.
 */
class SessionRepository(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _isLoggedIn = MutableStateFlow(loadIsLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _hasSeenOnboarding = MutableStateFlow(loadHasSeenOnboarding())
    val hasSeenOnboarding: StateFlow<Boolean> = _hasSeenOnboarding.asStateFlow()
    
    private val _currentUserId = MutableStateFlow(loadCurrentUserId())
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()
    
    /**
     * Guarda el estado de login del usuario.
     */
    fun setLoggedIn(isLoggedIn: Boolean, userId: String? = null) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
            .putString(KEY_CURRENT_USER_ID, userId)
            .apply()
        _isLoggedIn.value = isLoggedIn
        _currentUserId.value = userId
    }
    
    /**
     * Marca que el usuario ya vio el onboarding.
     */
    fun setOnboardingComplete() {
        prefs.edit().putBoolean(KEY_HAS_SEEN_ONBOARDING, true).apply()
        _hasSeenOnboarding.value = true
    }
    
    /**
     * Resetea el estado de onboarding (para testing).
     */
    fun resetOnboarding() {
        prefs.edit().putBoolean(KEY_HAS_SEEN_ONBOARDING, false).apply()
        _hasSeenOnboarding.value = false
    }
    
    /**
     * Cierra sesión del usuario.
     */
    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putString(KEY_CURRENT_USER_ID, null)
            .apply()
        _isLoggedIn.value = false
        _currentUserId.value = null
    }
    
    /**
     * Limpia todos los datos de sesión.
     */
    fun clearAll() {
        prefs.edit().clear().apply()
        _isLoggedIn.value = false
        _hasSeenOnboarding.value = false
        _currentUserId.value = null
    }
    
    private fun loadIsLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    private fun loadHasSeenOnboarding(): Boolean {
        return prefs.getBoolean(KEY_HAS_SEEN_ONBOARDING, false)
    }
    
    private fun loadCurrentUserId(): String? {
        return prefs.getString(KEY_CURRENT_USER_ID, null)
    }
    
    companion object {
        private const val PREFS_NAME = "unitask_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
    }
}
