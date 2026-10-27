package org.ucb.appp1.download

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

@Composable
fun DownloadScreen() {
    val context = LocalContext.current.applicationContext

    val preferences = remember {
        context.getSharedPreferences(
            "download_preferences",
            Context.MODE_PRIVATE
        )
    }

    val workManager = remember {
        WorkManager.getInstance(context)
    }

    // Conserva el identificador para volver a observar el trabajo.
    var workId by remember {
        val savedId = preferences.getString("last_work_id", null)

        mutableStateOf(
            savedId?.let { id ->
                runCatching { UUID.fromString(id) }.getOrNull()
            }
        )
    }

    var state by remember {
        mutableStateOf<WorkInfo.State?>(null)
    }

    var lastDownload by remember {
        mutableLongStateOf(
            preferences.getLong("last_download", 0L)
        )
    }

    var simulateRetry by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(workId) {
        val currentId = workId ?: return@LaunchedEffect

        workManager.getWorkInfoByIdFlow(currentId).collect { info ->
            state = info?.state

            if (info?.state == WorkInfo.State.SUCCEEDED) {
                lastDownload = preferences.getLong("last_download", 0L)
            }
        }
    }

    val workIsActive = state == WorkInfo.State.ENQUEUED ||
            state == WorkInfo.State.RUNNING ||
            state == WorkInfo.State.BLOCKED

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "Ejercicio 2: Descarga",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.size(20.dp))

            Text(
                text = "Estado: ${state?.name ?: "Sin iniciar"}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = if (lastDownload == 0L) {
                    "Última descarga: aún no realizada"
                } else {
                    "Última descarga: ${formatDownloadDate(lastDownload)}"
                }
            )

            Spacer(modifier = Modifier.size(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = simulateRetry,
                    onCheckedChange = { simulateRetry = it },
                    enabled = !workIsActive
                )

                Text("Simular fallo en el primer intento")
            }

            Spacer(modifier = Modifier.size(12.dp))

            Button(
                onClick = {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()

                    val request =
                        OneTimeWorkRequestBuilder<DownloadWorker>()
                            .setConstraints(constraints)
                            .setBackoffCriteria(
                                BackoffPolicy.EXPONENTIAL,
                                10,
                                TimeUnit.SECONDS
                            )
                            .setInputData(
                                workDataOf(
                                    "simulate_retry" to simulateRetry
                                )
                            )
                            .build()

                    // Muestra ENQUEUED mientras WorkManager registra la tarea.
                    state = WorkInfo.State.ENQUEUED

                    preferences.edit()
                        .putString("last_work_id", request.id.toString())
                        .apply()

                    workId = request.id
                    workManager.enqueue(request)
                },
                enabled = !workIsActive,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar descarga")
            }

            Spacer(modifier = Modifier.size(10.dp))

            OutlinedButton(
                onClick = {
                    workId?.let { id ->
                        workManager.cancelWorkById(id)
                    }
                },
                enabled = workId != null && workIsActive,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }

            Spacer(modifier = Modifier.size(16.dp))

            Text(
                text = "La descarga necesita conexión a internet."
            )
        }
    }
}

private fun formatDownloadDate(timestamp: Long): String {
    return SimpleDateFormat(
        "dd/MM/yyyy HH:mm:ss",
        Locale.getDefault()
    ).format(Date(timestamp))
}