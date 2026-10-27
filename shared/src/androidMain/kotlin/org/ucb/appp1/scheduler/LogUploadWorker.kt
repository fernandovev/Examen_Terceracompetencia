package org.ucb.appp1.scheduler

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class LogUploadWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): androidx.work.ListenableWorker.Result {
        println("ejecutar instruccion para subir datos")
        return androidx.work.ListenableWorker.Result.success()
    }
}
