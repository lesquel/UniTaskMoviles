package com.example.unitask.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Worker reservado para mostrar notificaciones puntuales si la lógica principal falla.
 */
class NotificationWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        // TODO: implementar fallback que use NotificationHelper cuando no se pudo entregar la notificación normal.
        return Result.success()
    }
}
