package com.example.unitask.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unitask.di.AppModule
import com.example.unitask.domain.model.NotificationSetting
import com.example.unitask.presentation.viewmodel.AlarmViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun AlarmSettingsScreen(
    viewModel: AlarmViewModel = viewModel(factory = AppModule.viewModelFactory),
    onBack: () -> Unit
) {
    val items by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingSetting by remember { mutableStateOf<NotificationSetting?>(null) }

    fun openCreateDialog() {
        editingSetting = null
        showDialog = true
    }

    fun openEditDialog(setting: NotificationSetting) {
        editingSetting = setting
        showDialog = true
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Alarmas y recordatorios",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Define cuándo quieres recibir una notificación antes del evento (24h, 1h, 5m o 1m).",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                item {
                    Text(
                        text = "Alarmas guardadas",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(items, key = { it.id }) { setting ->
                    AlarmRow(
                        settings = setting,
                        onEdit = { openEditDialog(setting) },
                        onDelete = { viewModel.cancel(setting.id) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (items.isEmpty()) {
                    item {
                        Text(
                            text = "No hay alarmas guardadas",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onBack) {
                    Text(text = "Volver")
                }
                FloatingActionButton(onClick = ::openCreateDialog) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Añadir alarma")
                }
            }
        }
    }

    if (showDialog) {
        AlarmEditDialog(
            initial = editingSetting,
            onDismiss = { showDialog = false },
            onSave = { setting ->
                viewModel.schedule(setting)
                showDialog = false
            }
        )
    }
}

@Composable
private fun AlarmRow(
    settings: NotificationSetting,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm").withZone(ZoneId.systemDefault())
    val triggerText = fmt.format(Instant.ofEpochMilli(settings.triggerAtMillis))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Notificación próxima", style = MaterialTheme.typography.titleMedium)
                    Text(text = triggerText, style = MaterialTheme.typography.bodySmall)
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Editar alarma")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Eliminar alarma")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = describeInterval(settings), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Exacta: ${settings.exact}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun describeInterval(setting: NotificationSetting): String {
    val totalMillis = setting.repeatIntervalMillis ?: return "Alarma única"
    val minutes = totalMillis / 60000
    return if (minutes >= 60) {
        val hrs = minutes / 60
        "Recordatorio cada $hrs ${if (hrs == 1L) "hora" else "horas"}"
    } else {
        "Recordatorio cada $minutes ${if (minutes == 1L) "minuto" else "minutos"}"
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun AlarmEditDialog(
    initial: NotificationSetting?,
    onDismiss: () -> Unit,
    onSave: (NotificationSetting) -> Unit
) {
    var enabled by remember { mutableStateOf(initial?.enabled ?: true) }
    var amount by remember { mutableStateOf(extractInitialAmount(initial)) }
    var exact by remember { mutableStateOf(initial?.exact ?: false) }
    var unitIsMinutes by remember { mutableStateOf(initial?.useMinutes ?: true) }
    var amountError by remember { mutableStateOf<String?>(null) }

    val presets = listOf(
        ReminderPreset("24h antes", 24, false),
        ReminderPreset("1h antes", 1, false),
        ReminderPreset("5m antes", 5, true),
        ReminderPreset("1m antes", 1, true)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (amount !in 1..24) {
                    amountError = "Escoge un valor entre 1 y 24."
                    return@TextButton
                }
                val unitMillis = if (unitIsMinutes) 60000L else 3600000L
                val trigger = System.currentTimeMillis() + amount * unitMillis
                val id = initial?.id ?: UUID.randomUUID().toString()
                val setting = NotificationSetting(
                    id = id,
                    taskId = initial?.taskId,
                    enabled = enabled,
                    triggerAtMillis = trigger,
                    repeatIntervalMillis = amount * unitMillis,
                    useMinutes = unitIsMinutes,
                    exact = exact
                )
                onSave(setting)
            }) {
                Text(text = "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Cancelar") }
        },
        title = { Text(text = if (initial == null) "Nueva alarma" else "Editar alarma") },
        text = {
            Column {
                Text(
                    text = "Programa cuántos minutos o horas antes del evento debe sonar la alarma.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { amount = (amount - 1).coerceIn(1, 24) }) {
                        Icon(imageVector = Icons.Filled.RemoveCircle, contentDescription = "Reducir")
                    }
                    Text(
                        text = "$amount",
                        fontSize = 24.sp,
                        modifier = Modifier.width(64.dp),
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = { amount = (amount + 1).coerceIn(1, 24) }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Aumentar")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = if (unitIsMinutes) "Minutos" else "Horas")
                }
                if (amountError != null) {
                    Text(text = amountError!!, color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Prácticos presets:")
                FlowRow(modifier = Modifier.padding(top = 6.dp)) {
                    presets.forEach { preset ->
                        FilterChip(
                            modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                            selected = amount == preset.amount && unitIsMinutes == preset.useMinutes,
                            onClick = {
                                amount = preset.amount
                                unitIsMinutes = preset.useMinutes
                                amountError = null
                            },
                            label = { Text(text = preset.label) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = enabled, onCheckedChange = { enabled = it })
                    Text(text = "Activa la alarma")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = exact, onCheckedChange = { exact = it })
                    Text(text = "Usar alarma exacta")
                }
            }
        }
    )
}

private fun extractInitialAmount(initial: NotificationSetting?): Int {
    if (initial == null) return 1
    val unitMillis = if (initial.useMinutes) 60000L else 3600000L
    return ((initial.triggerAtMillis - System.currentTimeMillis()) / unitMillis)
        .coerceIn(1, 24)
        .toInt()
}

private data class ReminderPreset(val label: String, val amount: Int, val useMinutes: Boolean)

