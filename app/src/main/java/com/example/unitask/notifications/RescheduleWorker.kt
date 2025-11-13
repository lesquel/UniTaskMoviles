package com.example.unitask.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.unitask.di.AppModule
import kotlinx.coroutines.flow.first

class RescheduleWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        try {
            // Ensure AppModule is configured (should be called from MainActivity normally)
            AppModule.configureAppModule(applicationContext)
            val repo = AppModule.provideNotificationRepository()
            val all = repo.observeAll().first()
            all.forEach { setting ->
                repo.schedule(setting)
            }
            return Result.success()
        } catch (t: Throwable) {
            return Result.failure()
        }
    }
}
