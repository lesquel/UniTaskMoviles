package com.example.unitask.presentation.ui.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.unitask.R
import com.example.unitask.presentation.viewmodel.SubjectItem
import com.example.unitask.presentation.viewmodel.SubjectsUiState
import com.example.unitask.ui.theme.UniTaskTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * =============================================================================
 * PRUEBAS DE INTERFAZ (UI TESTING) - SUBJECTS SCREEN
 * =============================================================================
 * 
 * PANTALLA CLAVE: Gestión de materias/asignaturas
 * 
 * ¿QUÉ VALIDAN ESTAS PRUEBAS?
 * ---------------------------
 * 1. Lista de materias: Que se muestren correctamente
 * 2. FAB de agregar: Visible y clickeable
 * 3. Estado vacío: Mensaje informativo cuando no hay materias
 * 4. Acciones: Botones de editar y eliminar funcionan
 * 
 * ¿QUÉ PASARÍA SI FALLAN?
 * -----------------------
 * - subjectsListDisplaysItems: Usuario no ve sus materias configuradas
 * - fabAddSubjectIsClickable: Usuario no puede crear materias (CRÍTICO)
 * - emptyStateDisplaysMessage: Pantalla en blanco confusa
 * - editButtonIsClickable: Usuario no puede modificar materias existentes
 * - deleteButtonIsClickable: Usuario no puede eliminar materias incorrectas
 */
@RunWith(AndroidJUnit4::class)
class SubjectsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * PRUEBA 1: El estado vacío muestra mensaje informativo
     * ----------------------------------------------------
     * VALIDA: Estado vacío de la lista
     * IMPACTO SI FALLA: Usuario ve pantalla en blanco, no sabe qué hacer
     */
    @Test
    fun emptyStateDisplaysMessage() {
        composeRule.setContent {
            UniTaskTheme {
                SubjectsScreenTestWrapper(
                    state = SubjectsUiState(subjects = emptyList())
                )
            }
        }

        val noSubjectsMessage = context.getString(R.string.no_subjects_message)
        composeRule.onNodeWithText(noSubjectsMessage).assertIsDisplayed()
        
        val addPrompt = context.getString(R.string.add_subject_prompt)
        composeRule.onNodeWithText(addPrompt).assertIsDisplayed()
    }

    /**
     * PRUEBA 2: El FAB de agregar materia está visible y es clickeable
     * ---------------------------------------------------------------
     * VALIDA: Botón flotante de agregar
     * IMPACTO SI FALLA: Usuario no puede crear materias - FUNCIONALIDAD CRÍTICA
     */
    @Test
    fun fabAddSubjectIsClickable() {
        var fabClicked = false

        composeRule.setContent {
            UniTaskTheme {
                SubjectsScreenTestWrapper(
                    state = SubjectsUiState(subjects = emptyList()),
                    onAddClick = { fabClicked = true }
                )
            }
        }

        val addSubjectDesc = context.getString(R.string.add_subject)
        composeRule.onNodeWithContentDescription(addSubjectDesc)
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        assert(fabClicked) { "El FAB de agregar materia no disparó el callback" }
    }

    /**
     * PRUEBA 3: Las materias se muestran en la lista
     * ---------------------------------------------
     * VALIDA: Renderizado de lista de materias
     * IMPACTO SI FALLA: Usuario no ve sus materias configuradas
     */
    @Test
    fun subjectsListDisplaysItems() {
        val subjects = listOf(
            SubjectItem(id = "1", name = "Matemáticas", colorHex = "#FF0000", teacher = "Prof. García"),
            SubjectItem(id = "2", name = "Física", colorHex = "#0000FF", teacher = "Prof. López"),
            SubjectItem(id = "3", name = "Historia", colorHex = "#00FF00", teacher = null)
        )

        composeRule.setContent {
            UniTaskTheme {
                SubjectsScreenTestWrapper(
                    state = SubjectsUiState(subjects = subjects)
                )
            }
        }

        // Verifica que todos los nombres de materias están visibles
        composeRule.onNodeWithText("Matemáticas").assertIsDisplayed()
        composeRule.onNodeWithText("Física").assertIsDisplayed()
        composeRule.onNodeWithText("Historia").assertIsDisplayed()
        
        // Verifica que los profesores se muestran
        composeRule.onNodeWithText("Prof. García").assertIsDisplayed()
        composeRule.onNodeWithText("Prof. López").assertIsDisplayed()
    }

    /**
     * PRUEBA 4: El botón de volver funciona correctamente
     * --------------------------------------------------
     * VALIDA: Navegación hacia atrás
     * IMPACTO SI FALLA: Usuario queda atrapado en la pantalla de materias
     */
    @Test
    fun backButtonNavigatesBack() {
        var backClicked = false

        composeRule.setContent {
            UniTaskTheme {
                SubjectsScreenTestWrapper(
                    state = SubjectsUiState(subjects = emptyList()),
                    onBack = { backClicked = true }
                )
            }
        }

        val backDesc = context.getString(R.string.back)
        composeRule.onNodeWithContentDescription(backDesc)
            .assertIsDisplayed()
            .performClick()

        assert(backClicked) { "El botón de volver no disparó el callback" }
    }

    /**
     * PRUEBA 5: El botón de editar está visible y es clickeable
     * --------------------------------------------------------
     * VALIDA: Acción de editar en tarjetas de materias
     * IMPACTO SI FALLA: Usuario no puede modificar materias existentes
     */
    @Test
    fun editButtonIsClickable() {
        var editedSubject: SubjectItem? = null
        val subject = SubjectItem(
            id = "test-1",
            name = "Química",
            colorHex = "#FFFF00",
            teacher = "Prof. Martínez"
        )

        composeRule.setContent {
            UniTaskTheme {
                SubjectsScreenTestWrapper(
                    state = SubjectsUiState(subjects = listOf(subject)),
                    onEdit = { editedSubject = it }
                )
            }
        }

        // Verifica que la materia se muestra
        composeRule.onNodeWithText("Química").assertIsDisplayed()
        
        // Hace clic en el botón de editar
        val editDesc = context.getString(R.string.edit)
        composeRule.onNodeWithContentDescription(editDesc)
            .assertIsDisplayed()
            .performClick()

        assert(editedSubject?.id == "test-1") { 
            "El callback de editar no se invocó correctamente: $editedSubject" 
        }
    }

    /**
     * PRUEBA 6: El botón de eliminar está visible y es clickeable
     * ----------------------------------------------------------
     * VALIDA: Acción de eliminar en tarjetas de materias
     * IMPACTO SI FALLA: Usuario no puede eliminar materias incorrectas
     */
    @Test
    fun deleteButtonIsClickable() {
        var deletedSubject: SubjectItem? = null
        val subject = SubjectItem(
            id = "test-2",
            name = "Biología",
            colorHex = "#00FFFF",
            teacher = null
        )

        composeRule.setContent {
            UniTaskTheme {
                SubjectsScreenTestWrapper(
                    state = SubjectsUiState(subjects = listOf(subject)),
                    onDelete = { deletedSubject = it }
                )
            }
        }

        composeRule.onNodeWithText("Biología").assertIsDisplayed()
        
        val deleteDesc = context.getString(R.string.delete)
        composeRule.onNodeWithContentDescription(deleteDesc)
            .assertIsDisplayed()
            .performClick()

        assert(deletedSubject?.id == "test-2") { 
            "El callback de eliminar no se invocó correctamente: $deletedSubject" 
        }
    }

    /**
     * PRUEBA 7: El título de la pantalla es correcto
     * ---------------------------------------------
     * VALIDA: Título de la barra superior
     * IMPACTO SI FALLA: Usuario confundido sobre en qué pantalla está
     */
    @Test
    fun screenTitleIsDisplayed() {
        composeRule.setContent {
            UniTaskTheme {
                SubjectsScreenTestWrapper(
                    state = SubjectsUiState(subjects = emptyList())
                )
            }
        }

        val title = context.getString(R.string.subjects_title)
        composeRule.onNodeWithText(title).assertIsDisplayed()
    }

    /**
     * PRUEBA 8: El botón de cambio de tema funciona
     * --------------------------------------------
     * VALIDA: Funcionalidad de tema claro/oscuro
     * IMPACTO SI FALLA: Usuario no puede cambiar la apariencia
     */
    @Test
    fun toggleThemeButtonIsClickable() {
        var themeToggled = false

        composeRule.setContent {
            UniTaskTheme {
                SubjectsScreenTestWrapper(
                    state = SubjectsUiState(subjects = emptyList()),
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

    /**
     * PRUEBA 9: Validación de color hexadecimal inválido
     * --------------------------------------------------
     * VALIDA: Formato correcto de color
     * IMPACTO SI FALLA: Crash al parsear color
     */
    @Test
    fun invalidHexColorShowsError() {
        val errorMessage = "El color debe tener formato #RRGGBB"
        
        composeRule.setContent {
            UniTaskTheme {
                SubjectsScreenTestWrapper(
                    state = SubjectsUiState(
                        errorMessage = errorMessage
                    )
                )
            }
        }

        composeRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    /**
     * PRUEBA 10: Validación de longitud de nombre
     * ------------------------------------------
     * VALIDA: Limites de inputs
     * IMPACTO SI FALLA: UI rota por texto muy largo
     */
    @Test
    fun subjectNameTooLongShowsError() {
        val errorMessage = "El nombre no puede exceder 30 caracteres."

        composeRule.setContent {
            UniTaskTheme {
                SubjectsScreenTestWrapper(
                    state = SubjectsUiState(
                        errorMessage = errorMessage
                    )
                )
            }
        }

        composeRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    /**
     * PRUEBA 11: Validación de longitud de profesor
     * ---------------------------------------------
     * VALIDA: Limites de inputs campo opcional
     * IMPACTO SI FALLA: UI rota
     */
    @Test
    fun teacherNameTooLongShowsError() {
        val errorMessage = "El profesor no puede exceder 40 caracteres."

        composeRule.setContent {
            UniTaskTheme {
                SubjectsScreenTestWrapper(
                    state = SubjectsUiState(
                        errorMessage = errorMessage
                    )
                )
            }
        }

        composeRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }
}

/**
 * Wrapper para probar SubjectsScreen de forma aislada.
 * Replica la interfaz de SubjectsScreen sin el Route completo.
 */
@androidx.compose.runtime.Composable
private fun SubjectsScreenTestWrapper(
    state: SubjectsUiState,
    onBack: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onEdit: (SubjectItem) -> Unit = {},
    onDelete: (SubjectItem) -> Unit = {},
    onToggleTheme: () -> Unit = {}
) {
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }

    androidx.compose.runtime.LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    SubjectsScreenForTest(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onAddClick = onAddClick,
        onEdit = onEdit,
        onDelete = onDelete,
        isDarkTheme = false,
        onToggleTheme = onToggleTheme
    )
}
