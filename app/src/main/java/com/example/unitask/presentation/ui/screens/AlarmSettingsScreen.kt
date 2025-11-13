package com.example.unitask.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.unitask.di.AppModule
import com.example.unitask.domain.model.NotificationSetting
import com.example.unitask.presentation.viewmodel.AlarmViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID


@Composable
fun AlarmSettingsScreen(viewModel: AlarmViewModel = viewModel(factory = AppModule.viewModelFactory), onBack: () -> Unit) {
    val items by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingSetting by remember { mutableStateOf<NotificationSetting?>(null) }

    fun openCreateDialog() {
        editingSetting = null
        showDialog = true
    }

    fun openEditDialog(s: NotificationSetting) {
        editingSetting = s
        showDialog = true
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(items, key = { it.id }) { s ->
                AlarmRow(setting = s, onEdit = { openEditDialog(s) }, onDelete = { viewModel.cancel(s.id) })
            }
        }

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onBack) { Text(text = "Back") }
            FloatingActionButton(onClick = { openCreateDialog() }) { Icon(imageVector = Icons.Default.Add, contentDescription = "Add") }
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
private fun AlarmRow(setting: NotificationSetting, onEdit: () -> Unit, onDelete: () -> Unit) {
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())
    val whenText = fmt.format(Instant.ofEpochMilli(setting.triggerAtMillis))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(text = "${whenText} — ${if (setting.useMinutes) "minutes" else "hours"}")
            Text(text = "Enabled: ${setting.enabled}  Exact: ${setting.exact}")
        }
        Row {
            IconButton(onClick = onEdit) { Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete") }
        }
    }
}

@Composable
private fun AlarmEditDialog(initial: NotificationSetting?, onDismiss: () -> Unit, onSave: (NotificationSetting) -> Unit) {
    var enabled by remember { mutableStateOf(initial?.enabled ?: true) }
    var useMinutes by remember { mutableStateOf(initial?.useMinutes ?: true) }
    var amountText by remember { mutableStateOf( if (initial != null) {
        val unit = if (initial.useMinutes) 60000L else 3600000L
        ((initial.triggerAtMillis - System.currentTimeMillis()) / unit).coerceAtLeast(1).toString()
    } else "60") }
    var exact by remember { mutableStateOf(initial?.exact ?: false) }

    AlertDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(onClick = {
            val amount = amountText.toLongOrNull() ?: 1L
            val unitMillis = if (useMinutes) 60000L else 3600000L
            val trigger = System.currentTimeMillis() + amount * unitMillis
            val id = initial?.id ?: UUID.randomUUID().toString()
            val setting = NotificationSetting(
                id = id,
                taskId = null,
                enabled = enabled,
                triggerAtMillis = trigger,
                repeatIntervalMillis = amount * unitMillis,
                useMinutes = useMinutes,
                exact = exact
            )
            onSave(setting)
        }) { Text(text = "Save") }
    }, dismissButton = {
        TextButton(onClick = onDismiss) { Text(text = "Cancel") }
    }, title = { Text(text = if (initial == null) "New Alarm" else "Edit Alarm") }, text = {
        Column {
            OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Amount") })
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = useMinutes, onCheckedChange = { useMinutes = it })
                Text(text = "Use minutes")
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = enabled, onCheckedChange = { enabled = it })
                Text(text = "Enabled")
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = exact, onCheckedChange = { exact = it })
                Text(text = "Exact alarm")
            }
        }
    })
}

