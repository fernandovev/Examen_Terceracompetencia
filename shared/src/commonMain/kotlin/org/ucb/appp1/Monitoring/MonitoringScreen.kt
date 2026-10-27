package org.ucb.appp1.monitoring

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val CHANNEL_ID = "monitoring_updates"
private const val LAST_SYNC_KEY = "last_sync"

@Composable
fun MonitoringScreen() {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val scope = rememberCoroutineScope()

    val database = remember {
        MonitoringDatabase.getInstance(appContext)
    }

    val dao = remember {
        database.monitoringDao()
    }

    val history by remember(dao) {
        dao.observeHistory()
    }.collectAsState(initial = emptyList())

    val preferences = remember {
        appContext.getSharedPreferences("monitoring_preferences", Context.MODE_PRIVATE)
    }

    var lastSync by remember {
        mutableStateOf(preferences.getLong(LAST_SYNC_KEY, 0L))
    }

    var checking by remember {
        mutableStateOf(false)
    }

    var message by remember {
        mutableStateOf("Pulsa «Verificar ahora» para sincronizar.")
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Ejercicio 1: Monitoring",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = if (lastSync == 0L) {
                        "Última sincronización: aún no realizada"
                    } else {
                        "Última sincronización: ${formatDate(lastSync)}"
                    },
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    text = "Valor guardado: " +
                            (history.firstOrNull()?.value?.toString() ?: "sin datos")
                )

                Button(
                    onClick = {
                        scope.launch {
                            checking = true
                            message = "Verificando configuración..."

                            try {
                                val result = verifyMonitoring(
                                    context = appContext,
                                    dao = dao
                                )

                                lastSync = System.currentTimeMillis()

                                preferences.edit()
                                    .putLong(LAST_SYNC_KEY, lastSync)
                                    .apply()

                                message = result
                            } catch (exception: Exception) {
                                message = "No se pudo sincronizar: " +
                                        (exception.localizedMessage ?: "error desconocido")
                            } finally {
                                checking = false
                            }
                        }
                    },
                    enabled = !checking,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (checking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )

                        Spacer(modifier = Modifier.size(10.dp))
                    }

                    Text("Verificar ahora")
                }

                Text(text = message)

                Text(
                    text = "Historial de cambios",
                    style = MaterialTheme.typography.titleLarge
                )

                if (history.isEmpty()) {
                    Text("Todavía no hay registros.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = history,
                            key = { change -> change.id }
                        ) { change ->
                            Surface(
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Valor: ${change.value}")
                                    Text(formatDate(change.timestamp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun verifyMonitoring(
    context: Context,
    dao: MonitoringDao
): String {
    val remoteConfig = FirebaseRemoteConfig.getInstance()

    // Intervalo bajo para probar durante el examen.
    val settings = FirebaseRemoteConfigSettings.Builder()
        .setMinimumFetchIntervalInSeconds(0)
        .build()

    remoteConfig.setConfigSettingsAsync(settings).await()

    // Recupera y activa la configuración publicada.
    remoteConfig.fetchAndActivate().await()

    val remoteValue = remoteConfig.getBoolean("monitoring")

    val document = FirebaseFirestore.getInstance()
        .collection("configRemota")
        .document("actual")

    // Se consulta el servidor para no comparar con un dato en caché.
    val snapshot = document.get(Source.SERVER).await()

    val firestoreValue = snapshot.getBoolean("monitoring")

    // Firestore debe reflejar el valor de Remote Config.
    if (!snapshot.exists() || firestoreValue != remoteValue) {
        document.set(
            mapOf("monitoring" to remoteValue)
        ).await()
    }

    val latest = dao.getLatest()

    if (latest == null) {
        // La primera lectura establece el valor inicial.
        dao.insert(
            MonitoringChange(
                value = remoteValue,
                timestamp = System.currentTimeMillis()
            )
        )

        return "Valor inicial guardado: $remoteValue"
    }

    if (latest.value != remoteValue) {
        dao.insert(
            MonitoringChange(
                value = remoteValue,
                timestamp = System.currentTimeMillis()
            )
        )

        showMonitoringNotification(context)

        return "Configuración actualizada: $remoteValue"
    }

    return "Sin cambios. Valor actual: $remoteValue"
}

private fun showMonitoringNotification(context: Context) {
    val manager = context.getSystemService(
        Context.NOTIFICATION_SERVICE
    ) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Cambios de configuración",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        manager.createNotificationChannel(channel)
    }

    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Configuración actualizada")
        .setContentText("El valor de monitoring ha cambiado.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context).notify(1001, notification)
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat(
        "dd/MM/yyyy HH:mm:ss",
        Locale.getDefault()
    )

    return formatter.format(Date(timestamp))
}