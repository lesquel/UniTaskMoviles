package com.example.unitask.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val FOCUS_SENSOR_PREFS = "focus_sensor_preferences"
private val Context.focusSensorPreferences by preferencesDataStore(name = FOCUS_SENSOR_PREFS)

/**
 * Repositorio que persiste si las alertas de enfoque deben mostrarse entre sesiones.
 */
class FocusSensorSettingsRepository(private val context: Context) {
    companion object {
        private val KEY_FOCUS_ALERTS_ENABLED = booleanPreferencesKey("focus_sensor_alerts_enabled")
    }

    /**
     * Flow que emite el estado actual de la preferencia y que por defecto está activada.
     */
    val focusAlertsEnabled: Flow<Boolean> = context.focusSensorPreferences.data
        .map { prefs -> prefs[KEY_FOCUS_ALERTS_ENABLED] ?: true }

    /**
     * Actualiza la preferencia para habilitar o deshabilitar las alertas.
     */
    suspend fun setFocusAlertsEnabled(enabled: Boolean) {
        context.focusSensorPreferences.edit { prefs ->
            prefs[KEY_FOCUS_ALERTS_ENABLED] = enabled
        }
    }
}
