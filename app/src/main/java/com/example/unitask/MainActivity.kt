package com.example.unitask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.unitask.presentation.navigation.UniTaskApp
import com.example.unitask.ui.theme.UniTaskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = rememberSaveable { mutableStateOf(systemDark) }
            UniTaskTheme(darkTheme = isDarkTheme.value) {
                UniTaskApp(
                    isDarkTheme = isDarkTheme.value,
                    onToggleTheme = { isDarkTheme.value = !isDarkTheme.value }
                )
            }
        }
    }
}