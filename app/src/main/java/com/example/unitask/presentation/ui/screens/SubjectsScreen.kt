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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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

@Composable
fun SubjectsRoute(
    viewModel: SubjectsViewModel = viewModel(factory = AppModule.viewModelFactory),
    onBack: () -> Unit
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

    SubjectsScreen(
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
        onDelete = { subject -> subjectToDelete = subject }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectsScreen(
    state: SubjectsUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    onEdit: (SubjectItem) -> Unit,
    onDelete: (SubjectItem) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Asignaturas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir asignatura")
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
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

@Composable
private fun SubjectColorSwatch(colorHex: String) {
    val color = runCatching { Color(android.graphics.Color.parseColor(colorHex)) }.getOrElse { MaterialTheme.colorScheme.primary }
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        drawCircle(color = color)
    }
}

@Composable
private fun EmptySubjectsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Aún no tienes asignaturas", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Añade una para organizar tus tareas",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class SubjectDialogState(
    val id: String? = null,
    val name: String = "",
    val colorHex: String = "#FF6F61",
    val teacher: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
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
        title = { Text(text = if (state.id == null) "Nueva asignatura" else "Editar asignatura") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = "Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = colorHex,
                    onValueChange = { colorHex = it },
                    label = { Text(text = "Color (hex)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text(text = "Profesor/a (opcional)") },
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
                Text(text = "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmDeleteDialog(
    subject: SubjectItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Eliminar asignatura") },
        text = { Text(text = "¿Eliminar ${subject.name}? Se eliminarán sus tareas asociadas.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar")
            }
        }
    )
}
