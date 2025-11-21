package com.example.unitask.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.net.Uri
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.unitask.presentation.ui.components.FocusSensorBanner
import com.example.unitask.presentation.ui.screens.AddTaskRoute
import com.example.unitask.presentation.ui.screens.AlarmSettingsScreen
import com.example.unitask.presentation.ui.screens.DashboardRoute
import com.example.unitask.presentation.ui.screens.SubjectsRoute
import com.example.unitask.sensors.FocusSensorManager
import com.example.unitask.settings.FocusSensorSettingsRepository

// Navigation destinations that identify each screen in the Compose NavHost.
private sealed class UniTaskDestination(val route: String) {
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
}

/**
 * Root composable that hosts navigation plus the focus alert banner.
 */
@Composable
fun UniTaskApp(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    focusSensorManager: FocusSensorManager,
    focusSensorSettingsRepository: FocusSensorSettingsRepository
) {
    val focusState by focusSensorManager.state.collectAsState()
    val focusAlertsEnabled by focusSensorSettingsRepository.focusAlertsEnabled.collectAsState(initial = true)
    LaunchedEffect(focusAlertsEnabled) {
        focusSensorManager.setAlertsEnabled(focusAlertsEnabled)
    }
    val navController = rememberNavController()
    Box(modifier = modifier.fillMaxSize()) {
        UniTaskNavHost(
            navController = navController,
            modifier = Modifier.fillMaxSize(),
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            focusSensorSettingsRepository = focusSensorSettingsRepository
        )
        if (focusAlertsEnabled) {
            FocusSensorBanner(
                state = focusState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
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
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    focusSensorSettingsRepository: FocusSensorSettingsRepository
) {
    NavHost(
        navController = navController,
        startDestination = UniTaskDestination.Dashboard.route,
        modifier = modifier
    ) {
        composable(UniTaskDestination.Dashboard.route) {
            DashboardRoute(
                onAddTaskClick = { navController.navigate(UniTaskDestination.AddTask.route) },
                focusSensorSettingsRepository = focusSensorSettingsRepository,
                onTaskClick = { taskId -> navController.navigate(UniTaskDestination.AddTask.createRoute(taskId)) },
                onManageSubjectsClick = { navController.navigate(UniTaskDestination.Subjects.route) },
                onAlarmSettingsClick = { id -> navController.navigate(UniTaskDestination.AlarmSettings.createRoute(id)) },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
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
                onBack = { navController.popBackStack() },
                onAlarmSettingsClick = { navController.navigate(UniTaskDestination.AlarmSettings.route) }
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
    }
}
