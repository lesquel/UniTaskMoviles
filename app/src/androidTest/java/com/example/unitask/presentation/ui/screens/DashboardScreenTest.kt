package com.example.unitask.presentation.ui.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.unitask.presentation.viewmodel.DashboardUiState
import com.example.unitask.presentation.viewmodel.TaskUiModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dashboardShowsEmptyStateMessage() {
        composeRule.setContent {
            DashboardScreen(
                state = DashboardUiState(urgentTasks = emptyList(), allTasks = emptyList(), isLoading = false),
                snackbarHostState = SnackbarHostState(),
                onAddTaskClick = {},
                onManageSubjectsClick = {},
                onTaskCompleted = {}
            )
        }

        composeRule.onNodeWithText(composeTestRule.activity.getString(com.example.unitask.R.string.no_tasks_message)).assertIsDisplayed()
    }

    @Test
    fun dashboardDisplaysUrgentTaskTitle() {
        val sampleTask = TaskUiModel(
            id = "1",
            title = "Entrega de laboratorio",
            subjectName = "Química",
            subjectColorHex = "#FF0000",
            dueFormatted = "05 Nov 12:00",
            isCompleted = false
        )

        composeRule.setContent {
            DashboardScreen(
                state = DashboardUiState(urgentTasks = listOf(sampleTask), allTasks = listOf(sampleTask), isLoading = false),
                snackbarHostState = SnackbarHostState(),
                onAddTaskClick = {},
                onManageSubjectsClick = {},
                onTaskCompleted = {}
            )
        }

        composeRule.onNodeWithText("Entrega de laboratorio").assertIsDisplayed()
        composeRule.onNodeWithText("Urgente").assertIsDisplayed()
    }
}
