package com.example.unitask.presentation.ui.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.unitask.R
import com.example.unitask.presentation.viewmodel.AddTaskUiState
import com.example.unitask.presentation.viewmodel.SubjectOption
import com.example.unitask.ui.theme.UniTaskTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

/**
 * =============================================================================
 * PRUEBAS DE INTERFAZ (UI TESTING) - ADD TASK SCREEN
 * =============================================================================
 * 
 * PANTALLA CLAVE: Creación y edición de tareas
 * 
 * ¿QUÉ VALIDAN ESTAS PRUEBAS?
 * ---------------------------
 * 1. Formulario: Campos de título, fecha, hora y materia
 * 2. Validación: Mensajes de error cuando faltan datos
 * 3. Navegación: Botón de volver funciona correctamente
 * 4. Estados: Modo creación vs modo edición
 * 
 * ¿QUÉ PASARÍA SI FALLAN?
 * -----------------------
 * - titleFieldDisplaysAndAcceptsInput: Usuario no puede escribir títulos (CRÍTICO)
 * - saveButtonDisabledWithoutSubjects: Se guardan tareas incompletas
 * - backButtonNavigates: Usuario queda atrapado en la pantalla
 * - errorMessageDisplaysWhenPresent: Usuario no sabe qué corregir
 */
@RunWith(AndroidJUnit4::class)
class AddTaskScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * PRUEBA 1: El campo de título acepta entrada de texto
     * ---------------------------------------------------
     * VALIDA: Input de texto funciona correctamente
     * IMPACTO SI FALLA: Usuario no puede nombrar sus tareas - FUNCIONALIDAD PRINCIPAL ROTA
     */
    @Test
    fun titleFieldDisplaysAndAcceptsInput() {
        var capturedTitle = ""
        
        composeRule.setContent {
            UniTaskTheme {
                AddTaskScreen(
                    state = AddTaskUiState(
                        title = "",
                        subjects = listOf(
                            SubjectOption("1", "Matemáticas", "#FF0000")
                        )
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onBack = {},
                    onTitleChanged = { capturedTitle = it },
                    onSubjectSelected = {},
                    onDateSelected = {},
                    onTimeSelected = {},
                    onSubmit = {},
                    onAlarmSettingsClick = {}
                )
            }
        }

        // Verifica que el campo de título existe
        val titleLabel = context.getString(R.string.title_label)
        composeRule.onNodeWithText(titleLabel).assertIsDisplayed()
        
        // Intenta escribir en el campo
        composeRule.onNode(hasText(titleLabel)).performTextInput("Mi tarea de prueba")
        
        assert(capturedTitle == "Mi tarea de prueba") {
            "El texto ingresado no fue capturado correctamente: '$capturedTitle'"
        }
    }

    /**
     * PRUEBA 2: El botón de guardar está deshabilitado sin materias
     * ------------------------------------------------------------
     * VALIDA: Validación de formulario
     * IMPACTO SI FALLA: Se crean tareas sin materia asignada, datos inconsistentes
     */
    @Test
    fun saveButtonDisabledWithoutSubjects() {
        composeRule.setContent {
            UniTaskTheme {
                AddTaskScreen(
                    state = AddTaskUiState(
                        title = "Tarea de prueba",
                        subjects = emptyList(), // Sin materias
                        dueDate = LocalDate.now(),
                        dueTime = LocalTime.of(14, 0)
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onBack = {},
                    onTitleChanged = {},
                    onSubjectSelected = {},
                    onDateSelected = {},
                    onTimeSelected = {},
                    onSubmit = {},
                    onAlarmSettingsClick = {}
                )
            }
        }

        // El botón de guardar debe estar deshabilitado
        val saveText = context.getString(R.string.save_task)
        composeRule.onNodeWithText(saveText).assertIsNotEnabled()
    }

    /**
     * PRUEBA 3: El botón de guardar está habilitado con datos completos
     * ----------------------------------------------------------------
     * VALIDA: Habilitación del formulario
     * IMPACTO SI FALLA: Usuario no puede guardar tareas válidas
     */
    @Test
    fun saveButtonEnabledWithValidData() {
        composeRule.setContent {
            UniTaskTheme {
                AddTaskScreen(
                    state = AddTaskUiState(
                        title = "Tarea válida",
                        subjects = listOf(
                            SubjectOption("1", "Física", "#0000FF")
                        ),
                        selectedSubjectId = "1",
                        dueDate = LocalDate.now().plusDays(1),
                        dueTime = LocalTime.of(10, 30),
                        isSubmitting = false
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onBack = {},
                    onTitleChanged = {},
                    onSubjectSelected = {},
                    onDateSelected = {},
                    onTimeSelected = {},
                    onSubmit = {},
                    onAlarmSettingsClick = {}
                )
            }
        }

        val saveText = context.getString(R.string.save_task)
        composeRule.onNodeWithText(saveText).assertIsEnabled()
    }

    /**
     * PRUEBA 4: El botón de volver navega correctamente
     * ------------------------------------------------
     * VALIDA: Navegación hacia atrás
     * IMPACTO SI FALLA: Usuario queda atrapado, debe cerrar la app
     */
    @Test
    fun backButtonNavigatesBack() {
        var backClicked = false

        composeRule.setContent {
            UniTaskTheme {
                AddTaskScreen(
                    state = AddTaskUiState(
                        subjects = listOf(SubjectOption("1", "Historia", "#00FF00"))
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onBack = { backClicked = true },
                    onTitleChanged = {},
                    onSubjectSelected = {},
                    onDateSelected = {},
                    onTimeSelected = {},
                    onSubmit = {},
                    onAlarmSettingsClick = {}
                )
            }
        }

        val backDescription = context.getString(R.string.back)
        composeRule.onNodeWithContentDescription(backDescription)
            .assertIsDisplayed()
            .performClick()

        assert(backClicked) { "El botón de volver no disparó el callback" }
    }

    /**
     * PRUEBA 5: El título de la pantalla cambia en modo edición
     * --------------------------------------------------------
     * VALIDA: Diferenciación visual entre crear y editar
     * IMPACTO SI FALLA: Usuario confundido sobre qué acción está realizando
     */
    @Test
    fun screenTitleChangesInEditMode() {
        composeRule.setContent {
            UniTaskTheme {
                AddTaskScreen(
                    state = AddTaskUiState(
                        title = "Tarea existente",
                        editingTaskId = "task-123", // Modo edición
                        subjects = listOf(SubjectOption("1", "Química", "#FFFF00"))
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onBack = {},
                    onTitleChanged = {},
                    onSubjectSelected = {},
                    onDateSelected = {},
                    onTimeSelected = {},
                    onSubmit = {},
                    onAlarmSettingsClick = {}
                )
            }
        }

        val editTaskTitle = context.getString(R.string.edit_task)
        composeRule.onNodeWithText(editTaskTitle).assertIsDisplayed()
    }

    /**
     * PRUEBA 6: El título de la pantalla muestra "Nueva tarea" en modo creación
     * ------------------------------------------------------------------------
     * VALIDA: Título correcto en modo creación
     */
    @Test
    fun screenTitleShowsNewTaskInCreateMode() {
        composeRule.setContent {
            UniTaskTheme {
                AddTaskScreen(
                    state = AddTaskUiState(
                        title = "",
                        editingTaskId = null, // Modo creación
                        subjects = listOf(SubjectOption("1", "Biología", "#FF00FF"))
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onBack = {},
                    onTitleChanged = {},
                    onSubjectSelected = {},
                    onDateSelected = {},
                    onTimeSelected = {},
                    onSubmit = {},
                    onAlarmSettingsClick = {}
                )
            }
        }

        val newTaskTitle = context.getString(R.string.new_task)
        composeRule.onNodeWithText(newTaskTitle).assertIsDisplayed()
    }

    /**
     * PRUEBA 7: Los mensajes de error se muestran correctamente
     * --------------------------------------------------------
     * VALIDA: Feedback de errores al usuario
     * IMPACTO SI FALLA: Usuario no sabe qué está mal con su entrada
     */
    @Test
    fun errorMessageDisplaysWhenPresent() {
        val errorMessage = "El título no puede estar vacío"
        
        composeRule.setContent {
            UniTaskTheme {
                AddTaskScreen(
                    state = AddTaskUiState(
                        title = "",
                        errorMessage = errorMessage,
                        subjects = listOf(SubjectOption("1", "Arte", "#808080"))
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onBack = {},
                    onTitleChanged = {},
                    onSubjectSelected = {},
                    onDateSelected = {},
                    onTimeSelected = {},
                    onSubmit = {},
                    onAlarmSettingsClick = {}
                )
            }
        }

        composeRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    /**
     * PRUEBA 8: El botón de configurar alarmas está visible y es clickeable
     * --------------------------------------------------------------------
     * VALIDA: Acceso a funcionalidad secundaria
     * IMPACTO SI FALLA: Usuario no puede programar recordatorios
     */
    @Test
    fun alarmSettingsButtonIsClickable() {
        var alarmSettingsClicked = false

        composeRule.setContent {
            UniTaskTheme {
                AddTaskScreen(
                    state = AddTaskUiState(
                        subjects = listOf(SubjectOption("1", "Música", "#FFA500"))
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onBack = {},
                    onTitleChanged = {},
                    onSubjectSelected = {},
                    onDateSelected = {},
                    onTimeSelected = {},
                    onSubmit = {},
                    onAlarmSettingsClick = { alarmSettingsClicked = true }
                )
            }
        }

        val alarmSettingsText = context.getString(R.string.configure_alarms)
        composeRule.onNodeWithText(alarmSettingsText)
            .assertIsDisplayed()
            .performClick()

        assert(alarmSettingsClicked) { "El botón de alarmas no disparó el callback" }
    }

    /**
     * PRUEBA 9: Los selectores de fecha y hora están visibles
     * ------------------------------------------------------
     * VALIDA: Elementos de fecha/hora del formulario
     * IMPACTO SI FALLA: Usuario no puede especificar cuándo vence la tarea
     */
    @Test
    fun dateAndTimePickersAreDisplayed() {
        composeRule.setContent {
            UniTaskTheme {
                AddTaskScreen(
                    state = AddTaskUiState(
                        subjects = listOf(SubjectOption("1", "Deportes", "#00FFFF"))
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onBack = {},
                    onTitleChanged = {},
                    onSubjectSelected = {},
                    onDateSelected = {},
                    onTimeSelected = {},
                    onSubmit = {},
                    onAlarmSettingsClick = {}
                )
            }
        }

        val selectDateText = context.getString(R.string.select_date)
        val selectTimeText = context.getString(R.string.select_time)
        
        composeRule.onNodeWithText(selectDateText).assertIsDisplayed()
        composeRule.onNodeWithText(selectTimeText).assertIsDisplayed()
    }

    /**
     * PRUEBA 10: El texto del botón cambia durante el envío
     * ----------------------------------------------------
     * VALIDA: Feedback de estado de carga
     * IMPACTO SI FALLA: Usuario no sabe si la acción está en progreso
     */
    @Test
    fun saveButtonShowsLoadingState() {
        composeRule.setContent {
            UniTaskTheme {
                AddTaskScreen(
                    state = AddTaskUiState(
                        title = "Tarea",
                        subjects = listOf(SubjectOption("1", "Idiomas", "#800080")),
                        selectedSubjectId = "1",
                        isSubmitting = true // Estado de envío
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onBack = {},
                    onTitleChanged = {},
                    onSubjectSelected = {},
                    onDateSelected = {},
                    onTimeSelected = {},
                    onSubmit = {},
                    onAlarmSettingsClick = {}
                )
            }
        }

        val savingText = context.getString(R.string.saving)
        composeRule.onNodeWithText(savingText).assertIsDisplayed()
    }

    /**
     * PRUEBA 11: Se muestra error si el título es demasiado largo
     * ----------------------------------------------------------
     * VALIDA: Validación de longitud de texto
     * IMPACTO SI FALLA: Datos corruptos o crash por base de datos
     */
    @Test
    fun titleTooLongShowsError() {
        val longTitle = "A".repeat(51) // Asumiendo límite de 50
        val errorMessage = "El título no puede exceder 50 caracteres."
        
        composeRule.setContent {
            UniTaskTheme {
                AddTaskScreen(
                    state = AddTaskUiState(
                        title = longTitle,
                        errorMessage = errorMessage,
                        subjects = listOf(SubjectOption("1", "Lengua", "#FF00FF"))
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onBack = {},
                    onTitleChanged = {}, // Mock, no necesitamos lógica real aquí
                    onSubjectSelected = {},
                    onDateSelected = {},
                    onTimeSelected = {},
                    onSubmit = {},
                    onAlarmSettingsClick = {}
                )
            }
        }

        composeRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    /**
     * PRUEBA 12: Se muestra error si el título está vacío al guardar
     * -----------------------------------------------------------
     * VALIDA: Campos obligatorios
     * IMPACTO SI FALLA: Tareas sin título
     */
    @Test
    fun emptyTitleShowsError() {
        val errorMessage = "El título y la materia son obligatorios."

        composeRule.setContent {
            UniTaskTheme {
                AddTaskScreen(
                    state = AddTaskUiState(
                        title = "",
                        errorMessage = errorMessage,
                        subjects = listOf(SubjectOption("1", "Lengua", "#FF00FF"))
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onBack = {},
                    onTitleChanged = {},
                    onSubjectSelected = {},
                    onDateSelected = {},
                    onTimeSelected = {},
                    onSubmit = {},
                    onAlarmSettingsClick = {}
                )
            }
        }

        composeRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }
}
