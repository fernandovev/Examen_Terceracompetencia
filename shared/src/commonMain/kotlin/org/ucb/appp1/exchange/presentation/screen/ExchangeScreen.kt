package org.ucb.appp1.exchange.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import org.ucb.appp1.exchange.presentation.viewmodel.ExchangeViewModel
import org.designsystem.project.components.buttons.MainButton

@Composable
fun ExchangeScreen(viewModel: ExchangeViewModel = koinViewModel()) {
    val dollars by viewModel.dollarList.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MainButton(
            text = "Guardar Dólar en Room",
            onClick = { viewModel.onSaveDollarClick() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Dólares guardados:", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(dollars) { dollar ->
                Text(text = "${dollar.name}: Bs. ${dollar.price}", fontSize = 16.sp)
            }
        }
    }
}
