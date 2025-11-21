package com.example.unitask.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.unitask.R
import androidx.compose.material3.SwitchDefaults

/**
 * Cuadro de diálogo que permite al usuario activar o desactivar las alertas del sensor de proximidad.
 */
@Composable
fun FocusSensorSettingsDialog(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit
) {
    // Diálogo que alterna la funcionalidad de alertas persistida en DataStore.
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.LightMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(text = stringResource(id = R.string.focus_settings_title))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(text = stringResource(id = R.string.focus_settings_toggle_label))
                    Switch(
                        checked = enabled,
                        onCheckedChange = onEnabledChange,
                        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                    )
                }
                Text(text = stringResource(id = R.string.focus_settings_summary))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest, colors = ButtonDefaults.textButtonColors()) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        dismissButton = {} // no secondary button
    )
}
