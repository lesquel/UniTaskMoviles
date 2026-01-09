package com.example.unitask.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unitask.di.AppModule
import com.example.unitask.presentation.viewmodel.SubjectItem
import com.example.unitask.presentation.viewmodel.SubjectsEvent
import com.example.unitask.presentation.viewmodel.SubjectsUiState
import com.example.unitask.presentation.viewmodel.SubjectsViewModel

/**
 * Orquesta el flujo de la pantalla de materias (listar, editar, borrar) y gestiona los diálogos.
 */
@Composable
fun SubjectsRoute(
    viewModel: SubjectsViewModel = viewModel(factory = AppModule.viewModelFactory),
    onBack: () -> Unit,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var dialogState by remember { mutableStateOf<SubjectDialogState?>(null) }
    var subjectToDelete by remember { mutableStateOf<SubjectItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SubjectsEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeError()
    }

    SubjectsScreenForTest(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onAddClick = { dialogState = SubjectDialogState() },
        onEdit = { subject ->
            dialogState = SubjectDialogState(
                id = subject.id,
                name = subject.name,
                colorHex = subject.colorHex,
                teacher = subject.teacher.orEmpty()
            )
        },
        onDelete = { subject -> subjectToDelete = subject },
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme
    )

    dialogState?.let { current ->
        SubjectDialog(
            state = current,
            onDismiss = { dialogState = null },
            onConfirm = { data ->
                if (data.id == null) {
                    viewModel.addSubject(data.name, data.colorHex, data.teacher.ifBlank { null })
                } else {
                    viewModel.editSubject(data.id, data.name, data.colorHex, data.teacher.ifBlank { null })
                }
                dialogState = null
            }
        )
    }

    subjectToDelete?.let { subject ->
        ConfirmDeleteDialog(
            subject = subject,
            onDismiss = { subjectToDelete = null },
            onConfirm = {
                viewModel.deleteSubject(subject.id, cascade = true)
                subjectToDelete = null
            }
        )
    }
}

/**
 * Versión testeable de SubjectsScreen que no depende de ViewModels ni diálogos internos.
 * Permite pruebas de UI aisladas con callbacks directos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreenForTest(
    state: SubjectsUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    onEdit: (SubjectItem) -> Unit,
    onDelete: (SubjectItem) -> Unit,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = com.example.unitask.R.string.subjects_title)) },
                navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = com.example.unitask.R.string.back))
                    }
                },
                actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                                contentDescription = stringResource(id = com.example.unitask.R.string.change_theme)
                            )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = com.example.unitask.R.string.add_subject))
            }
        }
    ) { innerPadding ->
        if (state.subjects.isEmpty()) {
            EmptySubjectsState(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.subjects, key = { it.id }) { subject ->
                    SubjectCard(
                        subject = subject,
                        onEdit = { onEdit(subject) },
                        onDelete = { onDelete(subject) }
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta que resume una materia con acción para editar y eliminar.
 */
@Composable
private fun SubjectCard(
    subject: SubjectItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SubjectColorSwatch(colorHex = subject.colorHex)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = subject.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    subject.teacher?.takeIf { it.isNotBlank() }?.let {
                        Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(id = com.example.unitask.R.string.edit))
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(id = com.example.unitask.R.string.delete))
                }
            }
        }
    }
}

/**
 * Círculo pequeño que pinta el color asociado a la materia.
 */
@Composable
private fun SubjectColorSwatch(colorHex: String) {
    val color = runCatching { Color(android.graphics.Color.parseColor(colorHex)) }.getOrElse { MaterialTheme.colorScheme.primary }
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        drawCircle(color = color)
    }
}

/**
 * Estado vacío visual que invita a crear una materia cuando no hay datos.
 */
@Composable
private fun EmptySubjectsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(id = com.example.unitask.R.string.no_subjects_message), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = com.example.unitask.R.string.add_subject_prompt),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Estado temporal usado por el diálogo para almacenar los campos editables.
 */
private data class SubjectDialogState(
    val id: String? = null,
    val name: String = "",
    val colorHex: String = "#FF6F61",
    val teacher: String = ""
)
 
/**
 * Diálogo reutilizable para crear o editar una materia, validando nombre y color.
 */
@Composable
private fun SubjectDialog(
    state: SubjectDialogState,
    onDismiss: () -> Unit,
    onConfirm: (SubjectDialogState) -> Unit
) {
    var name by remember(state.id) { mutableStateOf(state.name) }
    var colorHex by remember(state.id) { mutableStateOf(state.colorHex) }
    var teacher by remember(state.id) { mutableStateOf(state.teacher) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (state.id == null) stringResource(id = com.example.unitask.R.string.new_subject) else stringResource(id = com.example.unitask.R.string.edit_subject)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = stringResource(id = com.example.unitask.R.string.name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = colorHex,
                    onValueChange = { colorHex = it },
                    label = { Text(text = stringResource(id = com.example.unitask.R.string.color_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text(text = stringResource(id = com.example.unitask.R.string.teacher_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onConfirm(
                        SubjectDialogState(
                            id = state.id,
                            name = name.trim(),
                            colorHex = colorHex.trim().ifBlank { "#FF6F61" },
                            teacher = teacher.trim()
                        )
                    )
                }
            ) {
                Text(text = stringResource(id = com.example.unitask.R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = com.example.unitask.R.string.cancel))
            }
        }
    )
}

/**
 * Confirma la eliminación de una materia con texto contextual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmDeleteDialog(
    subject: SubjectItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = com.example.unitask.R.string.delete_subject_title)) },
        text = { Text(text = stringResource(id = com.example.unitask.R.string.delete_subject_confirm, subject.name)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = com.example.unitask.R.string.delete_label))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = com.example.unitask.R.string.cancel))
            }
        }
    )
}
