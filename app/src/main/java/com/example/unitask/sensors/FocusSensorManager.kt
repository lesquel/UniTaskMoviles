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
 * Estado combinando las lecturas de luz y proximidad para que la UI sepa si mostrar alertas.
 */
data class FocusSensorState(
    val isDark: Boolean = false,
    val isUserPresent: Boolean = false,
    val isDeviceInMotion: Boolean = false
)

/**
 * Gestiona los sensores de luz y proximidad para disparar notificaciones cuando el usuario debe enfocarse.
 */
class FocusSensorManager(
    private val context: Context,
    private val notificationHelper: NotificationHelper
) {
    private val sensorManager: SensorManager = (context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager)
        ?: throw IllegalStateException("SensorManager unavailable")
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val proximitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _state = MutableStateFlow(FocusSensorState())
    val state: StateFlow<FocusSensorState> = _state

    // Evita reentradas para que cada notificación se muestre solo una vez por activación.
    private var darkNotificationSent = false
    private var nearNotificationSent = false
    private var motionNotificationSent = false
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

    private val motionListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val acceleration = event.values.getOrNull(0)?.let { it * it } ?: 0f
            val delta = event.values.getOrNull(1)?.let { it * it } ?: 0f
            val gamma = event.values.getOrNull(2)?.let { it * it } ?: 0f
            val magnitude = kotlin.math.sqrt(acceleration + delta + gamma)
            val inMotion = magnitude > 2.5f
            updateMotionState(inMotion)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    /**
     * Registra los listeners si las alertas están habilitadas y aún no estamos escuchando.
     */
    fun start() {
        if (!alertsEnabled || isListening) return
        lightSensor?.let {
            sensorManager.registerListener(lightListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        proximitySensor?.let {
            sensorManager.registerListener(proximityListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        accelerometer?.let {
            sensorManager.registerListener(motionListener, it, SensorManager.SENSOR_DELAY_UI)
        }
        isListening = true
    }

    /**
     * Anula el registro de los listeners y marca que ya no hay muestreo activo.
     */
    fun stop() {
        if (!isListening) return
        sensorManager.unregisterListener(lightListener)
        sensorManager.unregisterListener(proximityListener)
        sensorManager.unregisterListener(motionListener)
        isListening = false
    }

    /**
     * Actualiza la bandera de oscuridad y dispara una notificación de enfoque solo cuando cambia.
     */
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

    /**
     * Actualiza la lectura de proximidad y notifica la presencia del usuario solo al entrar en rango.
     */
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

    /**
     * Detecta movimiento sostenido y publica una alerta para que el usuario deje el celular.
     */
    private fun updateMotionState(isInMotion: Boolean) {
        val current = _state.value
        if (current.isDeviceInMotion == isInMotion) return
        _state.value = current.copy(isDeviceInMotion = isInMotion)
        if (isInMotion && !motionNotificationSent) {
            showMotionNotification()
            motionNotificationSent = true
        } else if (!isInMotion) {
            motionNotificationSent = false
        }
    }

    // Habilita/deshabilita el muestreo y reinicia el estado cuando las alertas se apagan.
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

    /**
     * Limpia banderas y retorna el estado a valores iniciales.
     */
    private fun resetState() {
        darkNotificationSent = false
        nearNotificationSent = false
        motionNotificationSent = false
        _state.value = FocusSensorState()
    }

    /**
     * Convoca NotificationHelper para informar que el dispositivo está en movimiento.
     */
    private fun showMotionNotification() {
        notificationHelper.showReminderNotification(
            "focus-motion",
            context.getString(R.string.focus_alert_title),
            context.getString(R.string.focus_notification_motion_body),
            null
        )
    }
    /**
     * Convoca NotificationHelper para informar que el ambiente está oscuro.
     */
    private fun showDarkNotification() {
        notificationHelper.showReminderNotification(
            "focus-dark",
            context.getString(R.string.focus_alert_title),
            context.getString(R.string.focus_notification_dark_body),
            null
        )
    }

    /**
     * Dispara un recordatorio indicando que el usuario está cerca del dispositivo.
     */
    private fun showProximityNotification() {
        notificationHelper.showReminderNotification(
            "focus-proximity",
            context.getString(R.string.focus_alert_title),
            context.getString(R.string.focus_notification_proximity_body),
            null
        )
    }
}
