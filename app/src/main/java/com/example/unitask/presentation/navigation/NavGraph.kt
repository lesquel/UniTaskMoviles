package com.example.unitask.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import android.net.Uri
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.unitask.presentation.ui.components.BottomNavBar
import com.example.unitask.data.SessionRepository
import com.example.unitask.data.ThemePreferencesRepository
import com.example.unitask.presentation.ui.screens.AddTaskRoute
import com.example.unitask.presentation.ui.screens.AlarmSettingsScreen
import com.example.unitask.presentation.ui.screens.DashboardRoute
import com.example.unitask.presentation.ui.screens.LeaderboardScreen
import com.example.unitask.presentation.ui.screens.LoginRoute
import com.example.unitask.presentation.ui.screens.OnboardingScreen
import com.example.unitask.presentation.ui.screens.ProfileRoute
import com.example.unitask.presentation.ui.screens.RegisterRoute
import com.example.unitask.presentation.ui.screens.SettingsScreen
import com.example.unitask.presentation.ui.screens.SubjectsRoute

// Navigation destinations that identify each screen in the Compose NavHost.
private sealed class UniTaskDestination(val route: String) {
    object Onboarding : UniTaskDestination("onboarding")
    object Dashboard : UniTaskDestination("dashboard")
    object AddTask : UniTaskDestination("add-task") {
        const val ARG_TASK_ID = "taskId"
        val routeWithArgument: String = "$route?$ARG_TASK_ID={$ARG_TASK_ID}"
        fun createRoute(taskId: String? = null): String =
            if (taskId.isNullOrBlank()) route else "$route?$ARG_TASK_ID=${Uri.encode(taskId)}"
    }
    object Subjects : UniTaskDestination("subjects")
    object AlarmSettings : UniTaskDestination("alarm-settings") {
        const val ARG_TASK_ID = "taskId"
        val routeWithArgument: String = "$route?$ARG_TASK_ID={$ARG_TASK_ID}"
        fun createRoute(taskId: String? = null): String =
            if (taskId.isNullOrBlank()) route else "$route?$ARG_TASK_ID=${Uri.encode(taskId)}"
    }
    object Profile : UniTaskDestination("profile")
    object Leaderboard : UniTaskDestination("leaderboard")
    object Settings : UniTaskDestination("settings")
    object Login : UniTaskDestination("login")
    object Register : UniTaskDestination("register")
}

// Rutas que muestran la barra de navegación inferior
private val bottomNavRoutes = setOf(
    UniTaskDestination.Dashboard.route,
    UniTaskDestination.Subjects.route,
    UniTaskDestination.AddTask.route,
    UniTaskDestination.Profile.route,
    UniTaskDestination.Leaderboard.route
)

// Rutas que no requieren autenticación
private val publicRoutes = setOf(
    UniTaskDestination.Login.route,
    UniTaskDestination.Register.route,
    UniTaskDestination.Onboarding.route
)

/**
 * Root composable que aloja la navegación de la app.
 * Verifica el estado de sesión antes de mostrar cualquier pantalla.
 */
@Composable
fun UniTaskApp(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    themeRepository: ThemePreferencesRepository? = null,
    sessionRepository: SessionRepository? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("?")
    
    // Observar estado de sesión
    val isLoggedIn by sessionRepository?.isLoggedIn?.collectAsState() ?: androidx.compose.runtime.remember { 
        androidx.compose.runtime.mutableStateOf(false) 
    }
    val hasSeenOnboarding by sessionRepository?.hasSeenOnboarding?.collectAsState() ?: androidx.compose.runtime.remember { 
        androidx.compose.runtime.mutableStateOf(true) 
    }
    
    // Determinar la ruta inicial basada en el estado de sesión
    val startDestination = when {
        !hasSeenOnboarding -> UniTaskDestination.Onboarding.route
        !isLoggedIn -> UniTaskDestination.Login.route
        else -> UniTaskDestination.Dashboard.route
    }
    
    // Determina si mostrar la barra de navegación inferior
    val showBottomBar = currentRoute in bottomNavRoutes && isLoggedIn
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { destination ->
                        if (currentRoute != destination.route) {
                            navController.navigate(destination.route) {
                                // Pop hasta el inicio para evitar acumulación de pantallas
                                popUpTo(UniTaskDestination.Dashboard.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            UniTaskNavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize(),
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                themeRepository = themeRepository,
                sessionRepository = sessionRepository
            )
        }
    }
}

/**
 * Hosts the navigation graph and wires per-screen dependencies.
 */
@Composable
private fun UniTaskNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    themeRepository: ThemePreferencesRepository? = null,
    sessionRepository: SessionRepository? = null
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Pantalla de Onboarding
        composable(UniTaskDestination.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    sessionRepository?.setOnboardingComplete()
                    navController.navigate(UniTaskDestination.Login.route) {
                        popUpTo(UniTaskDestination.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(UniTaskDestination.Dashboard.route) {
            DashboardRoute(
                onAddTaskClick = { navController.navigate(UniTaskDestination.AddTask.route) },
                onTaskClick = { taskId -> navController.navigate(UniTaskDestination.AddTask.createRoute(taskId)) },
                onManageSubjectsClick = { navController.navigate(UniTaskDestination.Subjects.route) },
                onAlarmSettingsClick = { id -> navController.navigate(UniTaskDestination.AlarmSettings.createRoute(id)) },
                isDarkTheme = isDarkTheme,
                onToggleTheme = {
                    // Navegar a settings en lugar de toggle directo
                    navController.navigate(UniTaskDestination.Settings.route)
                }
            )
        }
        composable(
            route = UniTaskDestination.AddTask.routeWithArgument,
            arguments = listOf(
                navArgument(UniTaskDestination.AddTask.ARG_TASK_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString(UniTaskDestination.AddTask.ARG_TASK_ID)
            AddTaskRoute(
                taskId = taskId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(UniTaskDestination.Subjects.route) {
            SubjectsRoute(
                onBack = { navController.popBackStack() },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
        composable(
            route = UniTaskDestination.AlarmSettings.routeWithArgument,
            arguments = listOf(
                navArgument(UniTaskDestination.AlarmSettings.ARG_TASK_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString(UniTaskDestination.AlarmSettings.ARG_TASK_ID)
            AlarmSettingsScreen(
                initialTaskId = taskId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(UniTaskDestination.Profile.route) {
            ProfileRoute(
                onBack = { navController.popBackStack() },
                onLogout = {
                    sessionRepository?.logout()
                    navController.navigate(UniTaskDestination.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(UniTaskDestination.Leaderboard.route) {
            LeaderboardScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(UniTaskDestination.Login.route) {
            LoginRoute(
                onLoginSuccess = { userId ->
                    sessionRepository?.setLoggedIn(true, userId)
                    navController.navigate(UniTaskDestination.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(UniTaskDestination.Register.route)
                }
            )
        }
        composable(UniTaskDestination.Register.route) {
            RegisterRoute(
                onRegisterSuccess = { userId ->
                    sessionRepository?.setLoggedIn(true, userId)
                    navController.navigate(UniTaskDestination.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        composable(UniTaskDestination.Settings.route) {
            if (themeRepository != null) {
                SettingsScreen(
                    themeRepository = themeRepository,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
