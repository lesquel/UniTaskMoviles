package com.example.unitask.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unitask.di.AppModule
import com.example.unitask.presentation.ui.components.TaskCard
import com.example.unitask.presentation.viewmodel.DashboardUiState
import com.example.unitask.presentation.viewmodel.DashboardViewModel
import com.example.unitask.presentation.viewmodel.TaskUiModel

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel = viewModel(factory = AppModule.viewModelFactory),
    onAddTaskClick: () -> Unit = {},
    onManageSubjectsClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
            viewModel.consumeError()
        }
    }

    DashboardScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAddTaskClick = onAddTaskClick,
        onManageSubjectsClick = onManageSubjectsClick,
        onTaskCompleted = viewModel::onTaskCompleted
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DashboardScreen(
    state: DashboardUiState,
    snackbarHostState: SnackbarHostState,
    onAddTaskClick: () -> Unit,
    onManageSubjectsClick: () -> Unit,
    onTaskCompleted: (String) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "UniTask") },
                actions = {
                    IconButton(onClick = onManageSubjectsClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = "Gestionar asignaturas")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTaskClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir tarea")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                UrgentTasksSection(tasks = state.urgentTasks, onTaskCompleted = onTaskCompleted)
            }
            item {
                Text(
                    text = "Todas las tareas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (state.allTasks.isEmpty()) {
                item {
                    EmptyState()
                }
            } else {
                items(state.allTasks) { task ->
                    TaskCard(
                        task = task,
                        onTaskCompleted = onTaskCompleted,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun UrgentTasksSection(
    tasks: List<TaskUiModel>,
    onTaskCompleted: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Urgente",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (tasks.isEmpty()) {
            Text(
                text = "Sin entregas en las próximas 48 horas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(tasks) { task ->
                    TaskCard(
                        task = task,
                        onTaskCompleted = onTaskCompleted,
                        modifier = Modifier.width(260.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No hay tareas pendientes. Disfruta tu tiempo libre!",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
