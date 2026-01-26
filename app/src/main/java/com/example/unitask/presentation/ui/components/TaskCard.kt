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

/**
 * Tarjeta que resume información clave de la tarea y ofrece acciones rápidas.
 * Diseño mejorado con mejor espaciado y visual más atractivo.
 */
@Composable
fun TaskCard(
    task: TaskUiModel,
    modifier: Modifier = Modifier,
    onTaskCompleted: (String) -> Unit,
    onAlarmSettingsClick: (String) -> Unit,
    onTaskClick: (String) -> Unit
) {
    val subjectColor = runCatching { Color(parseColor(task.subjectColorHex)) }
        .getOrElse { MaterialTheme.colorScheme.primary }
    
    Card(
        modifier = modifier
            .padding(vertical = 6.dp)
            .fillMaxWidth()
            .clickable { onTaskClick(task.id) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        // Barra de color lateral
        Row {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(120.dp)
                    .background(subjectColor)
            )
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            ) {
                // Header con materia y fecha
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SubjectBadge(task.subjectName, task.subjectColorHex)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = task.dueFormatted,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Título de la tarea
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Info de alarmas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (task.alarmCount > 0) subjectColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (task.alarmCount > 0) {
                                "${task.alarmCount} recordatorio(s)"
                            } else {
                                stringResource(id = com.example.unitask.R.string.alarm_summary_none)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Botón de completar
                    if (!task.isCompleted) {
                        IconButton(
                            onClick = { onTaskCompleted(task.id) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(id = com.example.unitask.R.string.complete_task),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(id = com.example.unitask.R.string.complete_task),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Da formato legible a la próxima alarma si existe.
 */
private fun formatTrigger(triggerAtMillis: Long?): String {
    if (triggerAtMillis == null) return ""
    val formatter = DateTimeFormatter.ofPattern("dd MMM HH:mm", Locale.getDefault())
    val instant = Instant.ofEpochMilli(triggerAtMillis)
    return formatter.withZone(ZoneId.systemDefault()).format(instant)
}
/**
 * Badge compacta que muestra el nombre y color de la materia asociada.
 */
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
