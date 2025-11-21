package com.example.unitask.presentation.ui.components

import android.graphics.Color.parseColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.unitask.presentation.viewmodel.TaskUiModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TaskCard(
    task: TaskUiModel,
    modifier: Modifier = Modifier,
    onTaskCompleted: (String) -> Unit,
    onAlarmSettingsClick: (String) -> Unit,
    onTaskClick: (String) -> Unit
) {
    Card(
        modifier = modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .clickable { onTaskClick(task.id) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SubjectBadge(task.subjectName, task.subjectColorHex)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = task.dueFormatted, style = MaterialTheme.typography.labelMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    if (task.alarmCount > 0) {
                        Text(
                            text = stringResource(id = com.example.unitask.R.string.alarm_summary_next, formatTrigger(task.nextAlarmAtMillis)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (task.alarmCount > 1) {
                            Text(
                                text = stringResource(id = com.example.unitask.R.string.alarm_summary_count, task.alarmCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(id = com.example.unitask.R.string.alarm_summary_none),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = { onAlarmSettingsClick(task.id) }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = stringResource(id = com.example.unitask.R.string.alarm_settings_action)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                if (!task.isCompleted) {
                    IconButton(onClick = { onTaskCompleted(task.id) }) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = stringResource(id = com.example.unitask.R.string.complete_task))
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(id = com.example.unitask.R.string.complete_task),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun formatTrigger(triggerAtMillis: Long?): String {
    if (triggerAtMillis == null) return ""
    val formatter = DateTimeFormatter.ofPattern("dd MMM HH:mm", Locale.getDefault())
    val instant = Instant.ofEpochMilli(triggerAtMillis)
    return formatter.withZone(ZoneId.systemDefault()).format(instant)
}
@Composable
fun SubjectBadge(label: String, colorHex: String, modifier: Modifier = Modifier) {
    val color = runCatching { Color(parseColor(colorHex)) }.getOrElse { MaterialTheme.colorScheme.primary }
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), shape = RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color, shape = CircleShape)
        )
    Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = color)
    }
}
