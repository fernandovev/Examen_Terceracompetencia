package org.ucb.appp1.download

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class DownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Opción para demostrar que WorkManager puede reintentar.
        val simulateRetry = inputData.getBoolean("simulate_retry", false)

        if (simulateRetry && runAttemptCount == 0) {
            return Result.retry()
        }

        // Simula una descarga de dos segundos.
        delay(2_000)

        val preferences = applicationContext.getSharedPreferences(
            "download_preferences",
            Context.MODE_PRIVATE
        )

        val saved = preferences.edit()
            .putLong("last_download", System.currentTimeMillis())
            .commit()

        return if (saved) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}