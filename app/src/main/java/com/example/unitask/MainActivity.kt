package com.example.unitask

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.example.unitask.data.AppTheme
import com.example.unitask.data.SessionRepository
import com.example.unitask.data.ThemePreferencesRepository
import com.example.unitask.di.AppModule
import com.example.unitask.notifications.NotificationHelper
import com.example.unitask.presentation.navigation.UniTaskApp
import com.example.unitask.ui.theme.UniTaskTheme

class MainActivity : ComponentActivity() {
    
    private lateinit var themeRepository: ThemePreferencesRepository
    private lateinit var sessionRepository: SessionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Configura el contenedor manual de dependencias que usan ViewModels y repositorios.
            AppModule.configureAppModule(applicationContext)
            
            // Inicializar repositorios
            themeRepository = ThemePreferencesRepository(applicationContext)
            sessionRepository = SessionRepository(applicationContext)
            
            // Prepara canales de notificación antes de que puedan dispararse alertas.
            val notificationManager = getSystemService(NotificationManager::class.java)
            if (notificationManager != null) {
                val notificationHelper = NotificationHelper(applicationContext, notificationManager)
                notificationHelper.createChannels()
            }

            // Solicita permiso de notificaciones en tiempo de ejecución (Android 13+).
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val perm = Manifest.permission.POST_NOTIFICATIONS
                if (ContextCompat.checkSelfPermission(this@MainActivity, perm) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(perm), 1001)
                }
            }

            // Verifica permiso para agendar alarmas exactas (solo mostrar una vez)
            requestExactAlarmPermissionIfNeeded()

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error initializing app", e)
        }

        // Edge-to-edge para renderizar la UI bajo las barras del sistema.
        enableEdgeToEdge()
        setContent {
            // Observar cambios en las preferencias de tema
            val themeSettings by themeRepository.settings.collectAsState()
            val systemDark = isSystemInDarkTheme()
            
            // Determinar si usar tema oscuro basado en la configuración
            val isDarkTheme = when (themeSettings.theme) {
                AppTheme.SYSTEM -> systemDark
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }
            
            UniTaskTheme(settings = themeSettings) {
                UniTaskApp(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { 
                        // Alternar entre claro y oscuro
                        val newTheme = if (isDarkTheme) AppTheme.LIGHT else AppTheme.DARK
                        themeRepository.updateTheme(newTheme)
                    },
                    themeRepository = themeRepository,
                    sessionRepository = sessionRepository
                )
            }
        }
    }
    
    private fun requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val am = getSystemService(android.app.AlarmManager::class.java)
                if (am != null && !am.canScheduleExactAlarms()) {
                    // Solo pedir permiso si no lo ha pedido recientemente
                    val prefs = getSharedPreferences("unitask_prefs", MODE_PRIVATE)
                    val lastAsked = prefs.getLong("alarm_perm_asked", 0)
                    val now = System.currentTimeMillis()
                    // Solo preguntar una vez por día
                    if (now - lastAsked > 86400000L) {
                        prefs.edit().putLong("alarm_perm_asked", now).apply()
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        intent.data = Uri.parse("package:${packageName}")
                        startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error requesting alarm permission", e)
            }
        }
    }
}