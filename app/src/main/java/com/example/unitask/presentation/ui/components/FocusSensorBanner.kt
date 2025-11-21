package com.example.unitask.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.unitask.R
import com.example.unitask.sensors.FocusSensorState
import kotlinx.coroutines.delay

@Composable
fun FocusSensorBanner(
    state: FocusSensorState,
    modifier: Modifier = Modifier
) {
    val messageRes = when {
        state.isDark -> R.string.focus_banner_dark_message
        state.isUserPresent -> R.string.focus_banner_proximity_message
        else -> null
    }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(messageRes) {
        if (messageRes != null) {
            visible = true
            delay(5_000)
            visible = false
        } else {
            visible = false
        }
    }
    if (messageRes == null || !visible) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(PaddingValues(vertical = 12.dp, horizontal = 16.dp)),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Sensors,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = stringResource(id = messageRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
