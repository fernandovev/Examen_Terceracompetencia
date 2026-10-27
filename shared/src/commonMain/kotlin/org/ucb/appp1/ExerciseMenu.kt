package org.ucb.appp1

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ucb.appp1.download.DownloadScreen
import org.ucb.appp1.monitoring.MonitoringScreen

@Composable
fun ExerciseMenu() {
    // 0: menú, 1: monitoring, 2: descarga.
    var screen by rememberSaveable {
        mutableIntStateOf(0)
    }

    BackHandler(enabled = screen != 0) {
        screen = 0
    }

    MaterialTheme {
        when (screen) {
            1 -> MonitoringScreen()
            2 -> DownloadScreen()
            else -> {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Ejercicios de Desarrollo Móvil",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Button(
                            onClick = { screen = 1 },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ejercicio 1 · Monitoring · 75 pts")
                        }

                        Button(
                            onClick = { screen = 2 },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ejercicio 2 · WorkManager · 25 pts")
                        }
                    }
                }
            }
        }
    }
}