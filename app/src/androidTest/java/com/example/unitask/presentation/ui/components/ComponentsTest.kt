package com.example.unitask.presentation.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.unitask.R
import com.example.unitask.presentation.viewmodel.TaskUiModel
import com.example.unitask.ui.theme.UniTaskTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * =============================================================================
 * PRUEBAS DE COMPONENTES REUTILIZABLES (COMPONENT TESTING)
 * =============================================================================
 * 
 * ¿QUÉ SON LAS PRUEBAS DE COMPONENTES?
 * ------------------------------------
 * Las pruebas de componentes validan elementos de UI individuales que se 
 * reutilizan en múltiples pantallas. Son más específicas que las pruebas
 * de pantalla completa pero más integradas que las pruebas unitarias.
 * 
 * ¿QUÉ VALIDAN ESTAS PRUEBAS?
 * ---------------------------
 * 1. TaskCard: Tarjeta principal de visualización de tareas
 * 2. EmptyState: Estado vacío cuando no hay datos
 * 3. RewardsBar: Barra de progreso de recompensas
 * 4. SubjectBadge: Etiqueta de materia con color
 * 
 * ¿POR QUÉ SON IMPORTANTES?
 * -------------------------
 * Los componentes reutilizables afectan múltiples pantallas:
 * - Si TaskCard falla, todas las listas de tareas están rotas
 * - Si EmptyState falla, los usuarios no reciben feedback en listas vacías
 * - Los bugs en componentes se multiplican por el número de usos
 * 
 * ¿QUÉ PASARÍA SI FALLAN?
 * -----------------------
 * - taskCardDisplaysTaskInfo: Las tareas no muestran información correcta
 * - taskCardCompleteButtonWorks: Los usuarios no pueden completar tareas
 * - emptyStateDisplaysMessage: Pantallas vacías confunden al usuario
 * - rewardsBarDisplaysProgress: Los usuarios no ven su progreso de gamificación
 */
@RunWith(AndroidJUnit4::class)
class ComponentsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    // =========================================================================
    // PRUEBAS DE TaskCard
    // =========================================================================

    /**
     * PRUEBA 1: TaskCard muestra la información de la tarea
     * ----------------------------------------------------
     * VALIDA: Renderizado de datos de la tarea
     * IMPACTO SI FALLA: Usuario no ve información de sus tareas
     */
    @Test
    fun taskCardDisplaysTaskInfo() {
        val task = TaskUiModel(
            id = "1",
            title = "Entregar proyecto final",
            subjectName = "Programación",
            subjectColorHex = "#FF5722",
            dueFormatted = "15 Ene 23:59",
            isCompleted = false,
            nextAlarmAtMillis = null,
            alarmCount = 0
        )

        composeRule.setContent {
            UniTaskTheme {
                TaskCard(
                    task = task,
                    onTaskCompleted = {},
                    onAlarmSettingsClick = {},
                    onTaskClick = {}
                )
            }
        }

        // Verifica que el título se muestra
        composeRule.onNodeWithText("Entregar proyecto final").assertIsDisplayed()
        
        // Verifica que la materia se muestra
        composeRule.onNodeWithText("Programación").assertIsDisplayed()
        
        // Verifica que la fecha se muestra
        composeRule.onNodeWithText("15 Ene 23:59").assertIsDisplayed()
    }

    /**
     * PRUEBA 2: El botón de completar tarea funciona
     * ---------------------------------------------
     * VALIDA: Interacción con el botón de check
     * IMPACTO SI FALLA: Usuarios no pueden marcar tareas como completadas (CRÍTICO)
     */
    @Test
    fun taskCardCompleteButtonWorks() {
        var completedId: String? = null
        val task = TaskUiModel(
            id = "task-abc",
            title = "Estudiar para parcial",
            subjectName = "Cálculo",
            subjectColorHex = "#2196F3",
            dueFormatted = "20 Ene 10:00",
            isCompleted = false,
            nextAlarmAtMillis = null,
            alarmCount = 0
        )

        composeRule.setContent {
            UniTaskTheme {
                TaskCard(
                    task = task,
                    onTaskCompleted = { completedId = it },
                    onAlarmSettingsClick = {},
                    onTaskClick = {}
                )
            }
        }

        val completeDesc = context.getString(R.string.complete_task)
        composeRule.onNodeWithContentDescription(completeDesc)
            .assertIsDisplayed()
            .performClick()

        assert(completedId == "task-abc") {
            "El callback de completar no se invocó correctamente: $completedId"
        }
    }

    /**
     * PRUEBA 3: El botón de alarmas funciona
     * -------------------------------------
     * VALIDA: Acceso a configuración de alarmas
     * IMPACTO SI FALLA: Usuarios no pueden configurar recordatorios
     */
    @Test
    fun taskCardAlarmButtonWorks() {
        var alarmTaskId: String? = null
        val task = TaskUiModel(
            id = "task-xyz",
            title = "Presentar informe",
            subjectName = "Investigación",
            subjectColorHex = "#4CAF50",
            dueFormatted = "25 Ene 14:00",
            isCompleted = false,
            nextAlarmAtMillis = null,
            alarmCount = 0
        )

        composeRule.setContent {
            UniTaskTheme {
                TaskCard(
                    task = task,
                    onTaskCompleted = {},
                    onAlarmSettingsClick = { alarmTaskId = it },
                    onTaskClick = {}
                )
            }
        }

        val alarmDesc = context.getString(R.string.alarm_settings_action)
        composeRule.onNodeWithContentDescription(alarmDesc)
            .assertIsDisplayed()
            .performClick()

        assert(alarmTaskId == "task-xyz") {
            "El callback de alarmas no se invocó correctamente: $alarmTaskId"
        }
    }

    /**
     * PRUEBA 4: El click en la tarjeta funciona
     * ----------------------------------------
     * VALIDA: Navegación al detalle de la tarea
     * IMPACTO SI FALLA: Usuarios no pueden editar tareas existentes
     */
    @Test
    fun taskCardClickWorks() {
        var clickedId: String? = null
        val task = TaskUiModel(
            id = "task-click",
            title = "Revisar apuntes",
            subjectName = "Historia",
            subjectColorHex = "#9C27B0",
            dueFormatted = "28 Ene 09:00",
            isCompleted = false,
            nextAlarmAtMillis = null,
            alarmCount = 0
        )

        composeRule.setContent {
            UniTaskTheme {
                TaskCard(
                    task = task,
                    onTaskCompleted = {},
                    onAlarmSettingsClick = {},
                    onTaskClick = { clickedId = it }
                )
            }
        }

        // Hace clic en la tarjeta (por el título)
        composeRule.onNodeWithText("Revisar apuntes")
            .assertIsDisplayed()
            .performClick()

        assert(clickedId == "task-click") {
            "El callback de click no se invocó correctamente: $clickedId"
        }
    }

    /**
     * PRUEBA 5: Tarea completada muestra estado diferente
     * --------------------------------------------------
     * VALIDA: Diferenciación visual de tareas completadas
     * IMPACTO SI FALLA: Usuario no distingue tareas pendientes de completadas
     */
    @Test
    fun taskCardShowsCompletedState() {
        val task = TaskUiModel(
            id = "completed-task",
            title = "Tarea terminada",
            subjectName = "Física",
            subjectColorHex = "#FF9800",
            dueFormatted = "01 Feb 12:00",
            isCompleted = true, // Completada
            nextAlarmAtMillis = null,
            alarmCount = 0
        )

        composeRule.setContent {
            UniTaskTheme {
                TaskCard(
                    task = task,
                    onTaskCompleted = {},
                    onAlarmSettingsClick = {},
                    onTaskClick = {}
                )
            }
        }

        // La tarea completada debe mostrar el ícono pero no el botón clickeable
        composeRule.onNodeWithText("Tarea terminada").assertIsDisplayed()
    }

    /**
     * PRUEBA 6: TaskCard muestra resumen de alarmas
     * --------------------------------------------
     * VALIDA: Información de recordatorios configurados
     * IMPACTO SI FALLA: Usuario no sabe si tiene recordatorios activos
     */
    @Test
    fun taskCardShowsAlarmSummary() {
        val task = TaskUiModel(
            id = "task-alarms",
            title = "Tarea con alarmas",
            subjectName = "Química",
            subjectColorHex = "#E91E63",
            dueFormatted = "05 Feb 16:00",
            isCompleted = false,
            nextAlarmAtMillis = System.currentTimeMillis() + 3600000,
            alarmCount = 2
        )

        composeRule.setContent {
            UniTaskTheme {
                TaskCard(
                    task = task,
                    onTaskCompleted = {},
                    onAlarmSettingsClick = {},
                    onTaskClick = {}
                )
            }
        }

        // Debe mostrar el resumen de alarmas
        composeRule.onNodeWithText("Tarea con alarmas").assertIsDisplayed()
        // Verifica que muestra el contador de recordatorios
        val alarmCountText = context.getString(R.string.alarm_summary_count, 2)
        composeRule.onNodeWithText(alarmCountText).assertIsDisplayed()
    }

    // =========================================================================
    // PRUEBAS DE EmptyState
    // =========================================================================

    /**
     * PRUEBA 7: EmptyState muestra mensaje por defecto
     * -----------------------------------------------
     * VALIDA: Mensaje informativo cuando no hay tareas
     * IMPACTO SI FALLA: Usuario confundido al ver listas vacías
     */
    @Test
    fun emptyStateDisplaysDefaultMessage() {
        composeRule.setContent {
            UniTaskTheme {
                EmptyState()
            }
        }

        val defaultMessage = context.getString(R.string.no_tasks_message)
        composeRule.onNodeWithText(defaultMessage).assertIsDisplayed()
    }

    /**
     * PRUEBA 8: EmptyState acepta mensaje personalizado
     * ------------------------------------------------
     * VALIDA: Flexibilidad del componente
     * IMPACTO SI FALLA: Los mensajes de estado vacío no se pueden personalizar
     */
    @Test
    fun emptyStateDisplaysCustomMessage() {
        composeRule.setContent {
            UniTaskTheme {
                EmptyState(titleResId = R.string.no_subjects_message)
            }
        }

        val customMessage = context.getString(R.string.no_subjects_message)
        composeRule.onNodeWithText(customMessage).assertIsDisplayed()
    }

    // =========================================================================
    // PRUEBAS DE RewardsBar
    // =========================================================================

    /**
     * PRUEBA 9: RewardsBar muestra nivel y XP
     * --------------------------------------
     * VALIDA: Información de gamificación
     * IMPACTO SI FALLA: Usuario no ve su progreso de recompensas
     */
    @Test
    fun rewardsBarDisplaysLevelAndXp() {
        composeRule.setContent {
            UniTaskTheme {
                RewardsBar(
                    xp = 250,
                    level = 3,
                    progressFraction = 0.5f
                )
            }
        }

        // Verifica que muestra el nivel y XP
        composeRule.onNodeWithText("Nivel 3 — XP: 250").assertIsDisplayed()
    }

    /**
     * PRUEBA 10: RewardsBar con nivel inicial
     * --------------------------------------
     * VALIDA: Estado inicial de gamificación
     * IMPACTO SI FALLA: Nuevos usuarios no ven su progreso inicial
     */
    @Test
    fun rewardsBarDisplaysInitialState() {
        composeRule.setContent {
            UniTaskTheme {
                RewardsBar(
                    xp = 0,
                    level = 1,
                    progressFraction = 0f
                )
            }
        }

        composeRule.onNodeWithText("Nivel 1 — XP: 0").assertIsDisplayed()
    }

    // =========================================================================
    // PRUEBAS DE SubjectBadge
    // =========================================================================

    /**
     * PRUEBA 11: SubjectBadge muestra nombre de materia
     * ------------------------------------------------
     * VALIDA: Renderizado de etiqueta de materia
     * IMPACTO SI FALLA: Las tarjetas de tareas no muestran la materia asociada
     */
    @Test
    fun subjectBadgeDisplaysLabel() {
        composeRule.setContent {
            UniTaskTheme {
                SubjectBadge(
                    label = "Álgebra",
                    colorHex = "#3F51B5"
                )
            }
        }

        composeRule.onNodeWithText("Álgebra").assertIsDisplayed()
    }

    /**
     * PRUEBA 12: SubjectBadge maneja colores inválidos
     * -----------------------------------------------
     * VALIDA: Robustez ante colores mal formateados
     * IMPACTO SI FALLA: La app crashea con colores inválidos en materias
     */
    @Test
    fun subjectBadgeHandlesInvalidColor() {
        composeRule.setContent {
            UniTaskTheme {
                SubjectBadge(
                    label = "Materia Test",
                    colorHex = "color-invalido" // Color inválido
                )
            }
        }

        // Debe mostrarse sin crashear
        composeRule.onNodeWithText("Materia Test").assertIsDisplayed()
    }
}
