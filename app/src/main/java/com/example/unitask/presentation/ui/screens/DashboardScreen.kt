package com.example.unitask.presentation.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.animateContentSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unitask.di.AppModule
import com.example.unitask.presentation.ui.components.DayFilter
import com.example.unitask.presentation.ui.components.DayFilterChips
import com.example.unitask.presentation.ui.components.EmptyState
import com.example.unitask.presentation.ui.components.SwipeableTaskCard
import com.example.unitask.presentation.ui.components.TaskCard
import com.example.unitask.presentation.viewmodel.DashboardUiState
import com.example.unitask.presentation.viewmodel.DashboardViewModel
import com.example.unitask.presentation.viewmodel.RewardsViewModel
import com.example.unitask.presentation.viewmodel.TaskUiModel

/**
 * Envuelve la lógica del ViewModel y las interacciones para exponer la pantalla principal.
 */
@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel = viewModel(factory = AppModule.viewModelFactory),
    rewardsViewModel: RewardsViewModel = viewModel(factory = AppModule.viewModelFactory),
    onAddTaskClick: () -> Unit = {},
    onManageSubjectsClick: () -> Unit = {},
    onAlarmSettingsClick: (String) -> Unit = {},
    onTaskClick: (String) -> Unit = {},
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
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
        onAlarmSettingsClick = onAlarmSettingsClick,
        onTaskClick = onTaskClick,
        onTaskCompleted = viewModel::onTaskCompleted,
        onTaskDeleted = viewModel::onTaskDeleted,
        onDayFilterSelected = viewModel::onDayFilterSelected,
        onLoadMoreTasks = viewModel::loadNextPage,
        onRefresh = viewModel::refresh,
        rewardsViewModel = rewardsViewModel,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme
    )
}

/**
 * Pantalla principal que combina tareas urgentes, barra de recompensas y listado completo.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    snackbarHostState: SnackbarHostState,
    onAddTaskClick: () -> Unit,
    onManageSubjectsClick: () -> Unit,
    onAlarmSettingsClick: (String) -> Unit,
    onTaskClick: (String) -> Unit,
    onTaskCompleted: (String) -> Unit,
    onTaskDeleted: (String) -> Unit,
    onDayFilterSelected: (DayFilter) -> Unit,
    onLoadMoreTasks: () -> Unit,
    onRefresh: () -> Unit,
    rewardsViewModel: RewardsViewModel,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    val xp by rewardsViewModel.xp.collectAsStateWithLifecycle()
    val level by rewardsViewModel.level.collectAsStateWithLifecycle(initialValue = 1)
    DashboardScreenForTest(
        state = state,
        snackbarHostState = snackbarHostState,
        onAddTaskClick = onAddTaskClick,
        onManageSubjectsClick = onManageSubjectsClick,
        onAlarmSettingsClick = onAlarmSettingsClick,
        onTaskClick = onTaskClick,
        onTaskCompleted = onTaskCompleted,
        onTaskDeleted = onTaskDeleted,
        onDayFilterSelected = onDayFilterSelected,
        onLoadMoreTasks = onLoadMoreTasks,
        onRefresh = onRefresh,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme,
        xp = xp,
        level = level
    )
}

/**
 * Versión testeable del Dashboard que no depende de ViewModels.
 * Permite pruebas de UI aisladas con valores mock.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreenForTest(
    state: DashboardUiState,
    snackbarHostState: SnackbarHostState,
    onAddTaskClick: () -> Unit,
    onManageSubjectsClick: () -> Unit,
    onAlarmSettingsClick: (String) -> Unit,
    onTaskClick: (String) -> Unit,
    onTaskCompleted: (String) -> Unit,
    onTaskDeleted: (String) -> Unit = {},
    onDayFilterSelected: (DayFilter) -> Unit = {},
    onLoadMoreTasks: () -> Unit = {},
    onRefresh: () -> Unit = {},
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    xp: Int = 0,
    level: Int = 1
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = stringResource(id = com.example.unitask.R.string.title_unittask))
                        // Mostrar racha si es mayor a 0
                        if (state.currentStreak > 0) {
                            com.example.unitask.presentation.ui.components.StreakBadge(streak = state.currentStreak)
                        }
                    }
                },
                actions = {
                    // Botón de refresh manual
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar"
                        )
                    }
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                            contentDescription = stringResource(id = com.example.unitask.R.string.change_theme)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = com.example.unitask.R.string.add_task))
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        // Pull to refresh wrapper
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Lista desplazable que mezcla tareas urgentes, barra de recompensas y el listado completo.
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            item {
                UrgentTasksSection(
                    tasks = state.urgentTasks,
                    onTaskCompleted = onTaskCompleted,
                    onAlarmSettingsClick = onAlarmSettingsClick,
                    onTaskClick = onTaskClick
                )
            }
            item {
                // Reward bar showing XP and level
                val progress = (xp % (maxOf(1, level * 100))).toFloat() / (level * 100).toFloat()
                com.example.unitask.presentation.ui.components.RewardsBar(xp = xp, level = level, progressFraction = progress)
            }
            item {
                Text(
                    text = stringResource(id = com.example.unitask.R.string.all_tasks),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            // Filtros por día de la semana
            item {
                DayFilterChips(
                    selectedFilter = state.selectedDayFilter,
                    onFilterSelected = onDayFilterSelected,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (state.displayedTasks.isEmpty()) {
                item {
                    EmptyState()
                }
            } else {
                items(state.displayedTasks, key = { it.id }) { task ->
                    SwipeableTaskCard(
                        task = task,
                        onTaskCompleted = onTaskCompleted,
                        onTaskDeleted = onTaskDeleted,
                        onAlarmSettingsClick = onAlarmSettingsClick,
                        onTaskClick = onTaskClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    )
                }
                // Botón para cargar más tareas
                if (state.hasMorePages) {
                    item {
                        androidx.compose.material3.TextButton(
                            onClick = onLoadMoreTasks,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cargar más tareas")
                        }
                    }
                }
            }
            }
        }
    }
}

/**
 * Muestra tareas urgentes en fila horizontal; al tocar una tarjeta inicia la edición.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UrgentTasksSection(
    tasks: List<TaskUiModel>,
    onTaskCompleted: (String) -> Unit,
    onAlarmSettingsClick: (String) -> Unit,
    onTaskClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = com.example.unitask.R.string.urgent),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (tasks.isEmpty()) {
            Text(
                text = stringResource(id = com.example.unitask.R.string.no_deliveries_48h),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onTaskCompleted = onTaskCompleted,
                        onAlarmSettingsClick = onAlarmSettingsClick,
                        onTaskClick = onTaskClick,
                        modifier = Modifier
                            .width(260.dp)
                            .animateContentSize()
                    )
                }
            }
        }
    }
}

// EmptyState moved to presentation.ui.components.EmptyState
