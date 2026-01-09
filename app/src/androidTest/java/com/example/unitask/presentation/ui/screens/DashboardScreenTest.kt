package com.example.unitask.presentation.ui.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.unitask.R
import com.example.unitask.presentation.viewmodel.DashboardUiState
import com.example.unitask.presentation.viewmodel.TaskUiModel
import com.example.unitask.ui.theme.UniTaskTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * =============================================================================
 * PRUEBAS DE INTERFAZ (UI TESTING) - DASHBOARD SCREEN
 * =============================================================================
 * 
 * ¿QUÉ SON LAS PRUEBAS DE INTERFAZ?
 * ---------------------------------
 * Las pruebas de UI validan que los elementos visuales se muestren correctamente
 * y que las interacciones del usuario funcionen como se espera. Se ejecutan en
 * un dispositivo real o emulador porque necesitan el contexto completo de Android.
 * 
 * ¿QUÉ VALIDAN ESTAS PRUEBAS?
 * ---------------------------
 * 1. Botones: Que existan, sean visibles y respondan a clics
 * 2. Textos: Que los mensajes correctos se muestren al usuario
 * 3. Navegación: Que los callbacks de navegación se invoquen
 * 4. Estados: Estado vacío, estado con datos, estado de error
 * 
 * ¿POR QUÉ SON IMPORTANTES EN APPS MÓVILES?
 * ------------------------------------------
 * - Detectan regresiones visuales antes de que lleguen a producción
 * - Validan la experiencia de usuario de manera automatizada
 * - Reducen el tiempo de pruebas manuales de QA
 * - Garantizan que la app funcione en diferentes configuraciones
 * 
 * ¿QUÉ PASARÍA SI FALLAN?
 * -----------------------
 * - dashboardShowsEmptyStateMessage: Usuario ve pantalla en blanco, confusión
 * - fabAddTaskIsDisplayedAndClickable: Usuario NO puede crear tareas (CRÍTICO)
 * - dashboardDisplaysUrgentTaskTitle: Usuario no ve tareas urgentes, pierde entregas
 * - dashboardDisplaysAllTasksHeader: UI incompleta, mala experiencia
 */
@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    // Contexto para obtener strings de recursos
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    // Helper para crear un ViewModel mock de recompensas
    private val mockXpFlow = MutableStateFlow(100)
    private val mockLevelFlow = MutableStateFlow(2)

    /**
     * PRUEBA 1: Estado vacío muestra mensaje informativo
     * --------------------------------------------------
     * VALIDA: Textos en estado vacío
     * IMPACTO SI FALLA: Usuario ve pantalla en blanco sin orientación
     */
    @Test
    fun dashboardShowsEmptyStateMessage() {
        composeRule.setContent {
            UniTaskTheme {
                DashboardScreenTestWrapper(
                    state = DashboardUiState(
                        urgentTasks = emptyList(),
                        allTasks = emptyList(),
                        isLoading = false
                    )
                )
            }
        }

        // Verifica que el mensaje de estado vacío se muestre
        val noTasksMessage = context.getString(R.string.no_tasks_message)
        composeRule.onNodeWithText(noTasksMessage).assertIsDisplayed()
    }

    /**
     * PRUEBA 2: FAB de agregar tarea está visible y es clickeable
     * -----------------------------------------------------------
     * VALIDA: Botón flotante (elemento crítico de navegación)
     * IMPACTO SI FALLA: Usuario NO puede crear nuevas tareas - FUNCIONALIDAD PRINCIPAL ROTA
     */
    @Test
    fun fabAddTaskIsDisplayedAndClickable() {
        var fabClicked = false

        composeRule.setContent {
            UniTaskTheme {
                DashboardScreenTestWrapper(
                    state = DashboardUiState(
                        urgentTasks = emptyList(),
                        allTasks = emptyList(),
                        isLoading = false
                    ),
                    onAddTaskClick = { fabClicked = true }
                )
            }
        }

        // Busca el FAB por su content description (accesibilidad)
        val addTaskDescription = context.getString(R.string.add_task)
        composeRule.onNodeWithContentDescription(addTaskDescription)
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        // Verifica que el callback se invocó
        assert(fabClicked) { "El FAB de agregar tarea no disparó el callback" }
    }

    /**
     * PRUEBA 3: Las tareas urgentes se muestran correctamente
     * -------------------------------------------------------
     * VALIDA: Renderizado de lista de tareas urgentes
     * IMPACTO SI FALLA: Usuario no ve entregas próximas, pierde deadlines
     */
    @Test
    fun dashboardDisplaysUrgentTaskTitle() {
        val sampleTask = TaskUiModel(
            id = "1",
            title = "Entrega de laboratorio",
            subjectName = "Química",
            subjectColorHex = "#FF0000",
            dueFormatted = "05 Nov 12:00",
            isCompleted = false,
            nextAlarmAtMillis = null,
            alarmCount = 0
        )

        composeRule.setContent {
            UniTaskTheme {
                DashboardScreenTestWrapper(
                    state = DashboardUiState(
                        urgentTasks = listOf(sampleTask),
                        allTasks = listOf(sampleTask),
                        isLoading = false
                    )
                )
            }
        }

        // Verifica título de la tarea
        composeRule.onNodeWithText("Entrega de laboratorio").assertIsDisplayed()
        
        // Verifica sección de urgentes
        val urgentText = context.getString(R.string.urgent)
        composeRule.onNodeWithText(urgentText).assertIsDisplayed()
    }

    /**
     * PRUEBA 4: El encabezado "Todas las tareas" se muestra
     * -----------------------------------------------------
     * VALIDA: Estructura visual del dashboard
     * IMPACTO SI FALLA: UI incompleta, confusión sobre qué sección es cuál
     */
    @Test
    fun dashboardDisplaysAllTasksHeader() {
        composeRule.setContent {
            UniTaskTheme {
                DashboardScreenTestWrapper(
                    state = DashboardUiState(
                        urgentTasks = emptyList(),
                        allTasks = emptyList(),
                        isLoading = false
                    )
                )
            }
        }

        val allTasksText = context.getString(R.string.all_tasks)
        composeRule.onNodeWithText(allTasksText).assertIsDisplayed()
    }

    /**
     * PRUEBA 5: El botón de materias es accesible
     * -------------------------------------------
     * VALIDA: Navegación secundaria
     * IMPACTO SI FALLA: Usuario no puede gestionar materias
     */
    @Test
    fun manageSubjectsButtonIsClickable() {
        var subjectsClicked = false

        composeRule.setContent {
            UniTaskTheme {
                DashboardScreenTestWrapper(
                    state = DashboardUiState(
                        urgentTasks = emptyList(),
                        allTasks = emptyList(),
                        isLoading = false
                    ),
                    onManageSubjectsClick = { subjectsClicked = true }
                )
            }
        }

        val manageSubjectsDesc = context.getString(R.string.manage_subjects)
        composeRule.onNodeWithContentDescription(manageSubjectsDesc)
            .assertIsDisplayed()
            .performClick()

        assert(subjectsClicked) { "El botón de materias no disparó el callback" }
    }

    /**
     * PRUEBA 6: Completar tarea invoca el callback
     * --------------------------------------------
     * VALIDA: Interacción con tarjetas de tareas
     * IMPACTO SI FALLA: Usuario no puede marcar tareas como completadas
     */
    @Test
    fun completeTaskButtonInvokesCallback() {
        var completedTaskId: String? = null
        
        val sampleTask = TaskUiModel(
            id = "task-123",
            title = "Estudiar para examen",
            subjectName = "Matemáticas",
            subjectColorHex = "#0000FF",
            dueFormatted = "10 Ene 14:00",
            isCompleted = false,
            nextAlarmAtMillis = null,
            alarmCount = 0
        )

        composeRule.setContent {
            UniTaskTheme {
                DashboardScreenTestWrapper(
                    state = DashboardUiState(
                        urgentTasks = emptyList(),
                        allTasks = listOf(sampleTask),
                        isLoading = false
                    ),
                    onTaskCompleted = { taskId -> completedTaskId = taskId }
                )
            }
        }

        // Verifica que la tarea se muestra
        composeRule.onNodeWithText("Estudiar para examen").assertIsDisplayed()

        // Busca y hace clic en el botón de completar (ícono de check)
        val completeDescription = context.getString(R.string.complete_task)
        composeRule.onNodeWithContentDescription(completeDescription)
            .performClick()

        assert(completedTaskId == "task-123") { 
            "El callback de completar tarea no se invocó correctamente. TaskId: $completedTaskId" 
        }
    }

    /**
     * PRUEBA 7: El mensaje de "no hay entregas en 48h" se muestra
     * ----------------------------------------------------------
     * VALIDA: Mensaje informativo en sección urgentes
     * IMPACTO SI FALLA: Usuario confundido sobre estado de urgentes
     */
    @Test
    fun noUrgentTasksShowsInformativeMessage() {
        composeRule.setContent {
            UniTaskTheme {
                DashboardScreenTestWrapper(
                    state = DashboardUiState(
                        urgentTasks = emptyList(),
                        allTasks = listOf(
                            TaskUiModel(
                                id = "1",
                                title = "Tarea futura",
                                subjectName = "Historia",
                                subjectColorHex = "#00FF00",
                                dueFormatted = "20 Ene 10:00",
                                isCompleted = false,
                                nextAlarmAtMillis = null,
                                alarmCount = 0
                            )
                        ),
                        isLoading = false
                    )
                )
            }
        }

        val noDeliveriesMessage = context.getString(R.string.no_deliveries_48h)
        composeRule.onNodeWithText(noDeliveriesMessage).assertIsDisplayed()
    }

    /**
     * PRUEBA 8: El botón de cambio de tema funciona
     * ---------------------------------------------
     * VALIDA: Funcionalidad de tema claro/oscuro
     * IMPACTO SI FALLA: Usuario no puede cambiar la apariencia
     */
    @Test
    fun toggleThemeButtonIsClickable() {
        var themeToggled = false

        composeRule.setContent {
            UniTaskTheme {
                DashboardScreenTestWrapper(
                    state = DashboardUiState(
                        urgentTasks = emptyList(),
                        allTasks = emptyList(),
                        isLoading = false
                    ),
                    onToggleTheme = { themeToggled = true }
                )
            }
        }

        val changeThemeDesc = context.getString(R.string.change_theme)
        composeRule.onNodeWithContentDescription(changeThemeDesc)
            .assertIsDisplayed()
            .performClick()

        assert(themeToggled) { "El botón de cambio de tema no disparó el callback" }
    }
}

/**
 * Wrapper que simula el DashboardScreen con valores mock para testing
 * Esto permite probar la UI sin necesidad de ViewModels reales
 */
@androidx.compose.runtime.Composable
private fun DashboardScreenTestWrapper(
    state: DashboardUiState,
    onAddTaskClick: () -> Unit = {},
    onManageSubjectsClick: () -> Unit = {},
    onAlarmSettingsClick: (String) -> Unit = {},
    onTaskClick: (String) -> Unit = {},
    onTaskCompleted: (String) -> Unit = {},
    onToggleTheme: () -> Unit = {}
) {
    // Mock de la barra de recompensas directamente
    val xpFlow = MutableStateFlow(100)
    val levelFlow = MutableStateFlow(2)
    
    DashboardScreenForTest(
        state = state,
        snackbarHostState = SnackbarHostState(),
        onAddTaskClick = onAddTaskClick,
        onManageSubjectsClick = onManageSubjectsClick,
        onAlarmSettingsClick = onAlarmSettingsClick,
        onTaskClick = onTaskClick,
        onTaskCompleted = onTaskCompleted,
        focusAlertsEnabled = false,
        onFocusAlertsToggle = {},
        isDarkTheme = false,
        onToggleTheme = onToggleTheme,
        xp = xpFlow.collectAsState().value,
        level = levelFlow.collectAsState().value
    )
}
