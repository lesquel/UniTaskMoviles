package com.example.unitask.presentation.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.foundation.layout.WindowInsets
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
import com.example.unitask.domain.model.AlarmTemplate
import com.example.unitask.presentation.viewmodel.AddTaskEvent
import com.example.unitask.presentation.viewmodel.AddTaskUiState
import com.example.unitask.presentation.viewmodel.AddTaskError
import com.example.unitask.presentation.viewmodel.AddTaskViewModel
import com.example.unitask.presentation.viewmodel.SubjectOption
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Configura ViewModel y efectos para la pantalla de creación/edición de tareas.
 */
@Composable
fun AddTaskRoute(
    taskId: String? = null,
    onBack: () -> Unit,
    onTaskSaved: () -> Unit = onBack
) {
    val viewModel: AddTaskViewModel = viewModel(factory = AppModule.addTaskViewModelFactory(taskId))
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddTaskEvent.Success -> {
                    val messageRes = if (event.isUpdate) com.example.unitask.R.string.task_updated else com.example.unitask.R.string.task_created
                    snackbarHostState.showSnackbar(
                        message = context.getString(messageRes),
                        duration = SnackbarDuration.Short
                    )
                    onTaskSaved()
                }
                is AddTaskEvent.Error -> {
                    val message = when (val error = event.error) {
                        is AddTaskError.SubmitError -> error.message
                        else -> context.getString(com.example.unitask.R.string.error_save_generic)
                    }
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    AddTaskScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        alarmTemplates = viewModel.alarmTemplates,
        onBack = onBack,
        onTitleChanged = viewModel::onTitleChanged,
        onSubjectSelected = viewModel::onSubjectSelected,
        onDateSelected = viewModel::onDateSelected,
        onTimeSelected = viewModel::onTimeSelected,
        onAlarmTemplateToggled = viewModel::onAlarmTemplateToggled,
        onSubmit = viewModel::submit
    )
}

/**
 * Formulario para capturar título, materia y fecha/hora de entrega con validación básica.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTaskScreen(
    state: AddTaskUiState,
    snackbarHostState: SnackbarHostState,
    alarmTemplates: List<AlarmTemplate> = emptyList(),
    onBack: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onSubjectSelected: (String) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    onAlarmTemplateToggled: (AlarmTemplate) -> Unit = {},
    onSubmit: () -> Unit
) {
    val errorMessage = state.error?.let { error ->
        when (error) {
            AddTaskError.TitleRequired -> stringResource(id = com.example.unitask.R.string.error_title_required)
            AddTaskError.TitleTooLong -> stringResource(id = com.example.unitask.R.string.error_title_max_len)
            AddTaskError.SubjectRequired -> stringResource(id = com.example.unitask.R.string.error_subject_required)
            AddTaskError.DateTimeRequired -> stringResource(id = com.example.unitask.R.string.error_datetime_required)
            is AddTaskError.SubmitError -> error.message
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    val isEditing = state.editingTaskId != null
    val context = LocalContext.current
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()) }
    val dueDateText = state.dueDate?.format(dateFormatter) ?: stringResource(id = com.example.unitask.R.string.select_date)
    val dueTimeText = state.dueTime?.format(timeFormatter) ?: stringResource(id = com.example.unitask.R.string.select_time)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            com.example.unitask.presentation.ui.components.AppHeader(
                title = stringResource(id = if (isEditing) com.example.unitask.R.string.edit_task else com.example.unitask.R.string.new_task),
                currentStreak = state.currentStreak,
                showBackButton = true,
                onBackClick = onBack
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(id = com.example.unitask.R.string.title_label)) },
                singleLine = true,
                isError = state.error == AddTaskError.TitleRequired || state.error == AddTaskError.TitleTooLong,
                supportingText = {
                    Text(
                        text = "${state.title.length}/${AddTaskViewModel.MAX_TITLE_LENGTH}",
                        color = if (state.title.length > AddTaskViewModel.MAX_TITLE_LENGTH) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
                    val dialog = DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
                        },
                        current.year,
                        current.monthValue - 1,
                        current.dayOfMonth
                    )
                    // Bloquear fechas anteriores a hoy
                    dialog.datePicker.minDate = System.currentTimeMillis() - 1000
                    dialog.show()
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
            
            // Selector de plantillas de alarma
            if (alarmTemplates.isNotEmpty()) {
                AlarmTemplateSelector(
                    templates = alarmTemplates,
                    selectedTemplateIds = state.selectedAlarmTemplates,
                    onTemplateToggled = onAlarmTemplateToggled
                )
            }
            
            state.error?.let { error ->
                val errorText = when (error) {
                    AddTaskError.TitleRequired -> stringResource(id = com.example.unitask.R.string.error_title_required)
                    AddTaskError.TitleTooLong -> stringResource(id = com.example.unitask.R.string.error_title_too_long, AddTaskViewModel.MAX_TITLE_LENGTH)
                    AddTaskError.SubjectRequired -> stringResource(id = com.example.unitask.R.string.error_subject_required)
                    AddTaskError.DateTimeRequired -> stringResource(id = com.example.unitask.R.string.error_datetime_required)
                    is AddTaskError.SubmitError -> error.message
                }
                Text(text = errorText, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSubmitting && state.subjects.isNotEmpty()
            ) {
                Text(text = if (state.isSubmitting) stringResource(id = com.example.unitask.R.string.saving) else stringResource(id = if (isEditing) com.example.unitask.R.string.update_task else com.example.unitask.R.string.save_task))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
/**
 * Muestra una lista desplegable para seleccionar la materia asociada a la tarea.
 */
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

/**
 * Botones que abren selectores nativos de fecha y hora usando los valores formateados.
 */
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

/**
 * Componente reutilizable que combina icono, etiqueta y valor visible para un picker.
 */
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

/**
 * Punto de color circular que muestra el color asociado a una materia.
 */
@Composable
private fun SubjectColorDot(colorHex: String) {
    val color = runCatching { Color(android.graphics.Color.parseColor(colorHex)) }
        .getOrElse { MaterialTheme.colorScheme.primary }
    Canvas(modifier = Modifier.size(12.dp)) {
        drawCircle(color = color)
    }
}

/**
 * Selector de plantillas de alarma con chips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AlarmTemplateSelector(
    templates: List<AlarmTemplate>,
    selectedTemplateIds: Set<String>,
    onTemplateToggled: (AlarmTemplate) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(id = com.example.unitask.R.string.configure_alarms),
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = "Selecciona cuándo quieres recibir recordatorios",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            templates.forEach { template ->
                val isSelected = selectedTemplateIds.contains(template.id)
                FilterChip(
                    selected = isSelected,
                    onClick = { onTemplateToggled(template) },
                    label = { Text(template.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
        if (selectedTemplateIds.isNotEmpty()) {
            Text(
                text = "${selectedTemplateIds.size} recordatorio(s) seleccionado(s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
