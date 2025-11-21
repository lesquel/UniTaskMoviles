package com.example.unitask.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.unitask.notifications.NotificationHelper
import com.example.unitask.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Exposes the latest light/proximity state so the UI can surface focused alerts.
 */
data class FocusSensorState(
    val isDark: Boolean = false,
    val isUserPresent: Boolean = false
)

class FocusSensorManager(
    private val context: Context,
    private val notificationHelper: NotificationHelper
) {
    private val sensorManager: SensorManager = (context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager)
        ?: throw IllegalStateException("SensorManager unavailable")
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val proximitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private val _state = MutableStateFlow(FocusSensorState())
    val state: StateFlow<FocusSensorState> = _state

    private var darkNotificationSent = false
    private var nearNotificationSent = false
    private var isListening = false
    private var alertsEnabled = true

    private val lightThreshold = 40f

    private val lightListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val lux = event.values.getOrNull(0) ?: return
            val dark = lux < lightThreshold
            updateDarkState(dark)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    private val proximityListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val value = event.values.getOrNull(0) ?: return
            val near = proximitySensor?.let { value < it.maximumRange } ?: false
            updateProximityState(near)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    fun start() {
        if (!alertsEnabled || isListening) return
        lightSensor?.let {
            sensorManager.registerListener(lightListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        proximitySensor?.let {
            sensorManager.registerListener(proximityListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        isListening = true
    }

    fun stop() {
        if (!isListening) return
        sensorManager.unregisterListener(lightListener)
        sensorManager.unregisterListener(proximityListener)
        isListening = false
    }

    private fun updateDarkState(isDark: Boolean) {
        val current = _state.value
        if (current.isDark == isDark) return
        _state.value = current.copy(isDark = isDark)
        if (isDark) {
            if (!darkNotificationSent) {
                showDarkNotification()
                darkNotificationSent = true
            }
        } else {
            darkNotificationSent = false
        }
    }

    private fun updateProximityState(isNear: Boolean) {
        val current = _state.value
        if (current.isUserPresent == isNear) return
        _state.value = current.copy(isUserPresent = isNear)
        if (isNear) {
            if (!nearNotificationSent) {
                showProximityNotification()
                nearNotificationSent = true
            }
        } else {
            nearNotificationSent = false
        }
    }

    fun setAlertsEnabled(enabled: Boolean) {
        if (alertsEnabled == enabled) return
        alertsEnabled = enabled
        if (enabled) {
            start()
        } else {
            stop()
            resetState()
        }
    }

    private fun resetState() {
        darkNotificationSent = false
        nearNotificationSent = false
        _state.value = FocusSensorState()
    }

    private fun showDarkNotification() {
        notificationHelper.showReminderNotification(
            "focus-dark",
            context.getString(R.string.focus_alert_title),
            context.getString(R.string.focus_notification_dark_body),
            null
        )
    }

    private fun showProximityNotification() {
        notificationHelper.showReminderNotification(
            "focus-proximity",
            context.getString(R.string.focus_alert_title),
            context.getString(R.string.focus_notification_proximity_body),
            null
        )
    }
}
