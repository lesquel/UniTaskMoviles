package com.example.unitask.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.unitask.presentation.ui.screens.AddTaskRoute
import com.example.unitask.presentation.ui.screens.AlarmSettingsScreen
import com.example.unitask.presentation.ui.screens.DashboardRoute
import com.example.unitask.presentation.ui.screens.SubjectsRoute

private sealed class UniTaskDestination(val route: String) {
    object Dashboard : UniTaskDestination("dashboard")
    object AddTask : UniTaskDestination("add-task")
    object Subjects : UniTaskDestination("subjects")
    object AlarmSettings : UniTaskDestination("alarm-settings") {
        const val ARG_TASK_ID = "taskId"
        val routeWithArgument: String = "$route?$ARG_TASK_ID={$ARG_TASK_ID}"
        fun createRoute(taskId: String? = null): String =
            if (taskId.isNullOrBlank()) route else "$route?$ARG_TASK_ID=$taskId"
    }
}

@Composable
fun UniTaskApp(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    UniTaskNavHost(
        navController = navController,
        modifier = modifier,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme
    )
}

@Composable
private fun UniTaskNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = UniTaskDestination.Dashboard.route,
        modifier = modifier
    ) {
        composable(UniTaskDestination.Dashboard.route) {
            DashboardRoute(
                onAddTaskClick = { navController.navigate(UniTaskDestination.AddTask.route) },
                onManageSubjectsClick = { navController.navigate(UniTaskDestination.Subjects.route) },
                onAlarmSettingsClick = { id -> navController.navigate(UniTaskDestination.AlarmSettings.createRoute(id)) },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
        composable(UniTaskDestination.AddTask.route) {
            AddTaskRoute(
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
