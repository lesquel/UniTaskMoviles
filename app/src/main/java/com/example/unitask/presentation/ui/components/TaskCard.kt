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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.unitask.presentation.viewmodel.TaskUiModel

@Composable
fun TaskCard(
    task: TaskUiModel,
    modifier: Modifier = Modifier,
    onTaskCompleted: (String) -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SubjectBadge(task.subjectName, task.subjectColorHex)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = task.dueFormatted, style = MaterialTheme.typography.labelMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = task.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                if (!task.isCompleted) {
                    IconButton(onClick = { onTaskCompleted(task.id) }) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Completar tarea")
                    }
                } else {
                    Checkbox(checked = true, onCheckedChange = null)
                }
            }
        }
    }
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
