package com.example.unitask.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Barra horizontal que despliega nivel y un indicador lineal del progreso hacia el siguiente nivel.
 */
@Composable
fun RewardsBar(xp: Int, level: Int, progressFraction: Float) {
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text(text = "Nivel $level — XP: $xp")
        LinearProgressIndicator(progress = { progressFraction })
    }
}
