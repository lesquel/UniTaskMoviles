package com.example.unitask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.unitask.presentation.navigation.UniTaskApp
import com.example.unitask.ui.theme.UniTaskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniTaskTheme {
                UniTaskApp()
            }
        }
    }
}