package com.example.unitask.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Colores predefinidos para las asignaturas.
 */
val subjectColors = listOf(
    "#FF6B6B" to "Rojo",
    "#FF8E53" to "Naranja",
    "#FFD93D" to "Amarillo",
    "#6BCB77" to "Verde",
    "#4D96FF" to "Azul",
    "#9B59B6" to "Púrpura",
    "#E91E63" to "Rosa",
    "#00BCD4" to "Cian",
    "#795548" to "Marrón",
    "#607D8B" to "Gris Azul",
    "#FF5722" to "Naranja Oscuro",
    "#8BC34A" to "Verde Lima",
    "#3F51B5" to "Índigo",
    "#009688" to "Verde Azulado",
    "#F44336" to "Rojo Intenso",
    "#2196F3" to "Azul Claro"
)

/**
 * Selector de color visual con colores predefinidos.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Color de la asignatura",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            subjectColors.forEach { (colorHex, _) ->
                ColorOption(
                    colorHex = colorHex,
                    isSelected = selectedColor.equals(colorHex, ignoreCase = true),
                    onClick = { onColorSelected(colorHex) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Mostrar el color seleccionado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(MaterialTheme.shapes.small)
                .background(parseColor(selectedColor))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.small
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = selectedColor.uppercase(),
                style = MaterialTheme.typography.bodySmall,
                color = if (isColorDark(selectedColor)) Color.White else Color.Black
            )
        }
    }
}

/**
 * Opción individual de color.
 */
@Composable
private fun ColorOption(
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(parseColor(colorHex))
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Seleccionado",
                tint = if (isColorDark(colorHex)) Color.White else Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Convierte un string hexadecimal a Color de Compose.
 */
fun parseColor(colorHex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color.Gray
    }
}

/**
 * Determina si un color es oscuro para elegir texto contrastante.
 */
fun isColorDark(colorHex: String): Boolean {
    return try {
        val color = android.graphics.Color.parseColor(colorHex)
        val darkness = 1 - (0.299 * android.graphics.Color.red(color) + 
                          0.587 * android.graphics.Color.green(color) + 
                          0.114 * android.graphics.Color.blue(color)) / 255
        darkness >= 0.5
    } catch (e: Exception) {
        false
    }
}
