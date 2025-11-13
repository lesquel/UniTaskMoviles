package com.example.unitask.presentation.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unitask.di.AppModule
import com.example.unitask.presentation.viewmodel.AlarmViewModel

@Composable
fun AlarmSettingsScreen(viewModel: AlarmViewModel = viewModel(factory = AppModule.viewModelFactory), onBack: () -> Unit) {
    // TODO: implement UI to list, create and edit alarm settings
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Alarm Settings - TODO")
        Button(onClick = onBack) { Text(text = "Back") }
    }
}
