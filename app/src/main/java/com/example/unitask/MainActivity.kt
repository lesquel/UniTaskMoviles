package com.example.unitask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.unitask.ui.theme.UniTaskTheme
import com.example.unitask.presentation.ui.screens.DashboardRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniTaskTheme {
                DashboardRoute()
            }
        }
    }
}