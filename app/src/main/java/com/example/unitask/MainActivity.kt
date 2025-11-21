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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.example.unitask.di.AppModule
import com.example.unitask.notifications.NotificationHelper
import com.example.unitask.presentation.navigation.UniTaskApp
import com.example.unitask.sensors.FocusSensorManager
import com.example.unitask.ui.theme.UniTaskTheme
import com.example.unitask.settings.FocusSensorSettingsRepository

class MainActivity : ComponentActivity() {
    private lateinit var focusSensorManager: FocusSensorManager
    private val focusSensorSettingsRepository by lazy { FocusSensorSettingsRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure AppModule at app startup
        AppModule.configureAppModule(applicationContext)
        // Create notification channels early
        val notificationManager = getSystemService(NotificationManager::class.java)
            ?: throw IllegalStateException("NotificationManager unavailable")
        val notificationHelper = NotificationHelper(applicationContext, notificationManager)
        notificationHelper.createChannels()
        focusSensorManager = FocusSensorManager(applicationContext, notificationHelper)

        // Runtime permission: POST_NOTIFICATIONS (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val perm = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this@MainActivity, perm) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(perm), 1001)
            }
        }

        // Check exact alarm scheduling permission and open settings if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(android.app.AlarmManager::class.java)
            if (am != null && !am.canScheduleExactAlarms()) {
                // Ask user to allow exact alarms in settings
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    intent.data = Uri.parse("package:${packageName}")
                    startActivity(intent)
                } catch (_: Exception) {
                    // Fallback: open app notification settings
                    val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    i.data = Uri.fromParts("package", packageName, null)
                    startActivity(i)
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = rememberSaveable { mutableStateOf(systemDark) }
            UniTaskTheme(darkTheme = isDarkTheme.value) {
                UniTaskApp(
                    isDarkTheme = isDarkTheme.value,
                    onToggleTheme = { isDarkTheme.value = !isDarkTheme.value },
                    focusSensorManager = focusSensorManager,
                    focusSensorSettingsRepository = focusSensorSettingsRepository
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        focusSensorManager.start()
    }

    override fun onPause() {
        focusSensorManager.stop()
        super.onPause()
    }
}