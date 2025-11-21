package com.example.unitask.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val FOCUS_SENSOR_PREFS = "focus_sensor_preferences"
private val Context.focusSensorPreferences by preferencesDataStore(name = FOCUS_SENSOR_PREFS)

// Persists whether focus alert banners/notifications are enabled across sessions.
class FocusSensorSettingsRepository(private val context: Context) {
    companion object {
        private val KEY_FOCUS_ALERTS_ENABLED = booleanPreferencesKey("focus_sensor_alerts_enabled")
    }

    val focusAlertsEnabled: Flow<Boolean> = context.focusSensorPreferences.data
        .map { prefs -> prefs[KEY_FOCUS_ALERTS_ENABLED] ?: true }

    suspend fun setFocusAlertsEnabled(enabled: Boolean) {
        context.focusSensorPreferences.edit { prefs ->
            prefs[KEY_FOCUS_ALERTS_ENABLED] = enabled
        }
    }
}
