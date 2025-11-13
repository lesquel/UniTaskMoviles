package com.example.unitask.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.unitask.presentation.ui.screens.AddTaskRoute
import com.example.unitask.presentation.ui.screens.DashboardRoute
import com.example.unitask.presentation.ui.screens.SubjectsRoute

private enum class UniTaskDestination(val route: String) {
    Dashboard("dashboard"),
    AddTask("add-task"),
    Subjects("subjects")
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
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
        composable(UniTaskDestination.AddTask.route) {
            AddTaskRoute(
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
    }
}
