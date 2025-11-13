package com.example.unitask.presentation.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unitask.di.AppModule
import com.example.unitask.presentation.viewmodel.AddTaskEvent
import com.example.unitask.presentation.viewmodel.AddTaskUiState
import com.example.unitask.presentation.viewmodel.AddTaskViewModel
import com.example.unitask.presentation.viewmodel.SubjectOption
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AddTaskRoute(
    viewModel: AddTaskViewModel = viewModel(factory = AppModule.viewModelFactory),
    onBack: () -> Unit,
    onTaskSaved: () -> Unit = onBack
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddTaskEvent.Success -> {
                        snackbarHostState.showSnackbar(
                            message = stringResource(id = com.example.unitask.R.string.task_created),
                            duration = SnackbarDuration.Short
                        )
                    onTaskSaved()
                }
                is AddTaskEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    AddTaskScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onTitleChanged = viewModel::onTitleChanged,
        onSubjectSelected = viewModel::onSubjectSelected,
        onDateSelected = viewModel::onDateSelected,
        onTimeSelected = viewModel::onTimeSelected,
        onSubmit = viewModel::submit
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    state: AddTaskUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onSubjectSelected: (String) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    onSubmit: () -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()) }
    val dueDateText = state.dueDate?.format(dateFormatter) ?: stringResource(id = com.example.unitask.R.string.select_date)
    val dueTimeText = state.dueTime?.format(timeFormatter) ?: stringResource(id = com.example.unitask.R.string.select_time)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = com.example.unitask.R.string.new_task)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = com.example.unitask.R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(id = com.example.unitask.R.string.title_label)) },
                singleLine = true
            )
            SubjectSelector(
                subjects = state.subjects,
                selectedId = state.selectedSubjectId,
                onSubjectSelected = onSubjectSelected
            )
            DateTimePickers(
                dueDateText = dueDateText,
                dueTimeText = dueTimeText,
                onPickDate = {
                    val current = state.dueDate ?: LocalDate.now()
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
                        },
                        current.year,
                        current.monthValue - 1,
                        current.dayOfMonth
                    ).show()
                },
                onPickTime = {
                    val current = state.dueTime ?: LocalTime.now().withSecond(0).withNano(0)
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            onTimeSelected(LocalTime.of(hourOfDay, minute))
                        },
                        current.hour,
                        current.minute,
                        true
                    ).show()
                }
            )
            state.errorMessage?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSubmit,
                enabled = !state.isSubmitting && state.subjects.isNotEmpty(),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = if (state.isSubmitting) stringResource(id = com.example.unitask.R.string.saving) else stringResource(id = com.example.unitask.R.string.save_task))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectSelector(
    subjects: List<SubjectOption>,
    selectedId: String?,
    onSubjectSelected: (String) -> Unit
) {
    if (subjects.isEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(id = com.example.unitask.R.string.subject_label), style = MaterialTheme.typography.labelMedium)
            Text(
                text = stringResource(id = com.example.unitask.R.string.add_subject_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var expanded by rememberSaveable { mutableStateOf(false) }
    val selected = subjects.firstOrNull { it.id == selectedId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth(),
            label = { Text(text = stringResource(id = com.example.unitask.R.string.subject_label)) },
            placeholder = { Text(text = stringResource(id = com.example.unitask.R.string.select_subject_placeholder)) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            subjects.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option.name) },
                    leadingIcon = { SubjectColorDot(colorHex = option.colorHex) },
                    onClick = {
                        onSubjectSelected(option.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DateTimePickers(
    dueDateText: String,
    dueTimeText: String,
    onPickDate: () -> Unit,
    onPickTime: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PickerButton(
            label = "Fecha de entrega",
            value = dueDateText,
            icon = Icons.Default.CalendarMonth,
            onClick = onPickDate
        )
        PickerButton(
            label = "Hora de entrega",
            value = dueTimeText,
            icon = Icons.Default.Schedule,
            onClick = onPickTime
        )
    }
}

@Composable
private fun PickerButton(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label)
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = value)
        }
    }
}

@Composable
private fun SubjectColorDot(colorHex: String) {
    val color = runCatching { Color(android.graphics.Color.parseColor(colorHex)) }
        .getOrElse { MaterialTheme.colorScheme.primary }
    Canvas(modifier = Modifier.size(12.dp)) {
        drawCircle(color = color)
    }
}
