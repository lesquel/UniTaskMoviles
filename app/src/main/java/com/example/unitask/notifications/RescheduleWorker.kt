package com.example.unitask.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.unitask.di.AppModule
import kotlinx.coroutines.flow.first

/**
 * Worker que recalcula y rehace las alarmas almacenadas al iniciar el dispositivo.
 */
class RescheduleWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        try {
            // Garantiza que AppModule esté inicializado, como debería ocurrir desde MainActivity.
            AppModule.configureAppModule(applicationContext)
            val repo = AppModule.provideNotificationRepository()
            val all = repo.observeAll().first()
            val currentTime = System.currentTimeMillis()
            
            // Solo reprogramar alarmas futuras que estén habilitadas
            all.filter { setting -> 
                setting.enabled && setting.triggerAtMillis > currentTime 
            }.forEach { setting ->
                repo.schedule(setting)
            }
            return Result.success()
        } catch (t: Throwable) {
            return Result.failure()
        }
    }
}
