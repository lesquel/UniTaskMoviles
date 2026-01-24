package com.example.unitask.domain.model

import java.util.UUID

/**
 * Plantilla de alarma reutilizable.
 * Permite seleccionar configuraciones predefinidas al crear tareas.
 */
data class AlarmTemplate(
    val id: String = UUID.randomUUID().toString(),
    val name: String,           // "30 minutos antes", "1 hora antes"
    val minutesBefore: Int,     // Minutos antes de la fecha de entrega
    val isDefault: Boolean = false  // Plantillas predeterminadas del sistema
) {
    companion object {
        /**
         * Plantillas predeterminadas del sistema.
         */
        val defaults = listOf(
            AlarmTemplate(
                id = "default_5min",
                name = "5 minutos antes",
                minutesBefore = 5,
                isDefault = true
            ),
            AlarmTemplate(
                id = "default_10min",
                name = "10 minutos antes",
                minutesBefore = 10,
                isDefault = true
            ),
            AlarmTemplate(
                id = "default_30min",
                name = "30 minutos antes",
                minutesBefore = 30,
                isDefault = true
            ),
            AlarmTemplate(
                id = "default_1hour",
                name = "1 hora antes",
                minutesBefore = 60,
                isDefault = true
            ),
            AlarmTemplate(
                id = "default_2hours",
                name = "2 horas antes",
                minutesBefore = 120,
                isDefault = true
            ),
            AlarmTemplate(
                id = "default_1day",
                name = "1 día antes",
                minutesBefore = 1440,
                isDefault = true
            )
        )
    }
}
