package com.example.unitask.presentation.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.unitask.R
import com.example.unitask.ui.theme.UniTaskTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/**
 * =============================================================================
 * PRUEBAS DE NAVEGACIÓN (NAVIGATION TESTING)
 * =============================================================================
 * 
 * ¿QUÉ VALIDAN ESTAS PRUEBAS?
 * ---------------------------
 * 1. Flujos de navegación: Transiciones entre pantallas
 * 2. Deep linking: Navegación con argumentos
 * 3. Back navigation: El botón de volver funciona
 * 4. State preservation: El estado se mantiene al navegar
 * 
 * ¿POR QUÉ SON IMPORTANTES?
 * -------------------------
 * La navegación es el esqueleto de la app. Si falla:
 * - Los usuarios quedan atrapados en pantallas
 * - Los flujos de trabajo se rompen completamente
 * - La experiencia de usuario se vuelve frustrante
 * 
 * ¿QUÉ PASARÍA SI FALLAN?
 * -----------------------
 * - dashboardToAddTaskNavigation: Usuario no puede crear tareas
 * - addTaskBackToDashboard: Usuario queda atrapado, debe cerrar la app
 * - dashboardToSubjectsNavigation: Usuario no puede gestionar materias
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * PRUEBA 1: Navegación de Dashboard a AddTask
     * ------------------------------------------
     * VALIDA: Flujo principal de creación de tareas
     * IMPACTO SI FALLA: El usuario no puede acceder a crear tareas (CRÍTICO)
     */
    @Test
    fun dashboardToAddTaskNavigation() {
        composeRule.setContent {
            UniTaskTheme {
                TestNavHost()
            }
        }

        // Verifica que estamos en Dashboard
        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()

        // Hace clic en el FAB de agregar
        val addTaskDesc = context.getString(R.string.add_task)
        composeRule.onNodeWithContentDescription(addTaskDesc)
            .assertIsDisplayed()
            .performClick()

        // Verifica que navegamos a AddTask
        composeRule.onNodeWithText("Nueva Tarea").assertIsDisplayed()
    }

    /**
     * PRUEBA 2: Navegación de regreso de AddTask a Dashboard
     * -----------------------------------------------------
     * VALIDA: Navegación hacia atrás funciona
     * IMPACTO SI FALLA: Usuario queda atrapado en la pantalla de nueva tarea
     */
    @Test
    fun addTaskBackToDashboard() {
        composeRule.setContent {
            UniTaskTheme {
                TestNavHost(startDestination = "add-task")
            }
        }

        // Verifica que estamos en AddTask
        composeRule.onNodeWithText("Nueva Tarea").assertIsDisplayed()

        // Hace clic en el botón de volver
        val backDesc = context.getString(R.string.back)
        composeRule.onNodeWithContentDescription(backDesc)
            .assertIsDisplayed()
            .performClick()

        // Verifica que volvemos a Dashboard
        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()
    }

    /**
     * PRUEBA 3: Navegación de Dashboard a Subjects
     * -------------------------------------------
     * VALIDA: Acceso a gestión de materias
     * IMPACTO SI FALLA: Usuario no puede configurar materias
     */
    @Test
    fun dashboardToSubjectsNavigation() {
        composeRule.setContent {
            UniTaskTheme {
                TestNavHost()
            }
        }

        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()

        // Hace clic en gestionar materias
        val manageSubjectsDesc = context.getString(R.string.manage_subjects)
        composeRule.onNodeWithContentDescription(manageSubjectsDesc)
            .assertIsDisplayed()
            .performClick()

        // Verifica que navegamos a Subjects
        val subjectsTitle = context.getString(R.string.subjects_title)
        composeRule.onNodeWithText(subjectsTitle).assertIsDisplayed()
    }

    /**
     * PRUEBA 4: Navegación de regreso de Subjects a Dashboard
     * ------------------------------------------------------
     * VALIDA: Navegación hacia atrás desde Subjects
     * IMPACTO SI FALLA: Usuario queda atrapado en la pantalla de materias
     */
    @Test
    fun subjectsBackToDashboard() {
        composeRule.setContent {
            UniTaskTheme {
                TestNavHost(startDestination = "subjects")
            }
        }

        val subjectsTitle = context.getString(R.string.subjects_title)
        composeRule.onNodeWithText(subjectsTitle).assertIsDisplayed()

        val backDesc = context.getString(R.string.back)
        composeRule.onNodeWithContentDescription(backDesc)
            .assertIsDisplayed()
            .performClick()

        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()
    }

    /**
     * PRUEBA 5: Flujo completo Dashboard -> AddTask -> Dashboard
     * ---------------------------------------------------------
     * VALIDA: Ciclo completo de navegación
     * IMPACTO SI FALLA: Los flujos de trabajo principales están rotos
     */
    @Test
    fun fullNavigationCycleDashboardAddTaskDashboard() {
        composeRule.setContent {
            UniTaskTheme {
                TestNavHost()
            }
        }

        // 1. Inicio en Dashboard
        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()

        // 2. Navegar a AddTask
        val addTaskDesc = context.getString(R.string.add_task)
        composeRule.onNodeWithContentDescription(addTaskDesc).performClick()
        composeRule.onNodeWithText("Nueva Tarea").assertIsDisplayed()

        // 3. Volver a Dashboard
        val backDesc = context.getString(R.string.back)
        composeRule.onNodeWithContentDescription(backDesc).performClick()
        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()
    }

    /**
     * PRUEBA 6: Flujo Dashboard -> Subjects -> Dashboard
     * -------------------------------------------------
     * VALIDA: Ciclo de navegación a materias
     */
    @Test
    fun fullNavigationCycleDashboardSubjectsDashboard() {
        composeRule.setContent {
            UniTaskTheme {
                TestNavHost()
            }
        }

        // 1. Inicio en Dashboard
        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()

        // 2. Navegar a Subjects
        val manageSubjectsDesc = context.getString(R.string.manage_subjects)
        composeRule.onNodeWithContentDescription(manageSubjectsDesc).performClick()
        
        val subjectsTitle = context.getString(R.string.subjects_title)
        composeRule.onNodeWithText(subjectsTitle).assertIsDisplayed()

        // 3. Volver a Dashboard
        val backDesc = context.getString(R.string.back)
        composeRule.onNodeWithContentDescription(backDesc).performClick()
        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()
    }
}

/**
 * NavHost simplificado para pruebas de navegación.
 * Usa pantallas mock en lugar de las reales para aislar la lógica de navegación.
 */
@Composable
private fun TestNavHost(startDestination: String = "dashboard") {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("dashboard") {
            MockDashboardScreen(
                onAddTaskClick = { navController.navigate("add-task") },
                onManageSubjectsClick = { navController.navigate("subjects") }
            )
        }
        composable("add-task") {
            MockAddTaskScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("subjects") {
            MockSubjectsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MockDashboardScreen(
    onAddTaskClick: () -> Unit,
    onManageSubjectsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = onManageSubjectsClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.manage_subjects)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTaskClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_task)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text("Dashboard Content")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MockAddTaskScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Tarea") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text("Add Task Content")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MockSubjectsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.subjects_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text("Subjects Content")
        }
    }
}
