package com.example.unitask.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Enum para el tema de la aplicación.
 */
enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

/**
 * Colores de acento disponibles para personalizar la app.
 */
enum class AccentColor(val seed: Color, val displayName: String) {
    PURPLE(Color(0xFF6750A4), "Púrpura"),
    BLUE(Color(0xFF1976D2), "Azul"),
    GREEN(Color(0xFF388E3C), "Verde"),
    ORANGE(Color(0xFFE65100), "Naranja"),
    PINK(Color(0xFFD81B60), "Rosa"),
    TEAL(Color(0xFF00796B), "Turquesa"),
    RED(Color(0xFFD32F2F), "Rojo"),
    INDIGO(Color(0xFF303F9F), "Índigo")
}

/**
 * Configuración de tema persistente.
 */
data class ThemeSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val accentColor: AccentColor = AccentColor.PURPLE,
    val useDynamicColor: Boolean = true
)

/**
 * Repositorio para persistir y observar las preferencias de tema.
 */
class ThemePreferencesRepository(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<ThemeSettings> = _settings.asStateFlow()
    
    private fun loadSettings(): ThemeSettings {
        return try {
            val themeName = prefs.getString(KEY_THEME, AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
            val accentName = prefs.getString(KEY_ACCENT, AccentColor.PURPLE.name) ?: AccentColor.PURPLE.name
            val useDynamic = prefs.getBoolean(KEY_DYNAMIC, true)
            
            ThemeSettings(
                theme = try { AppTheme.valueOf(themeName) } catch (_: Exception) { AppTheme.SYSTEM },
                accentColor = try { AccentColor.valueOf(accentName) } catch (_: Exception) { AccentColor.PURPLE },
                useDynamicColor = useDynamic
            )
        } catch (_: Exception) {
            ThemeSettings()
        }
    }
    
    fun updateTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        _settings.value = _settings.value.copy(theme = theme)
    }
    
    fun updateAccentColor(color: AccentColor) {
        prefs.edit().putString(KEY_ACCENT, color.name).apply()
        _settings.value = _settings.value.copy(accentColor = color)
    }
    
    fun updateUseDynamicColor(useDynamic: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC, useDynamic).apply()
        _settings.value = _settings.value.copy(useDynamicColor = useDynamic)
    }
    
    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_THEME = "app_theme"
        private const val KEY_ACCENT = "accent_color"
        private const val KEY_DYNAMIC = "use_dynamic_color"
    }
}
