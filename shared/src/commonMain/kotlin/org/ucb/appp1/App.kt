package org.ucb.appp1

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import io.sentry.Sentry
import org.ucb.appp1.exchange.presentation.screen.ExchangeScreen
import org.ucb.appp1.movies.presentation.screen.DollarScreen
import org.ucb.appp1.signin.presentation.screen.LoginScreen
import org.ucb.appp1.userinformation.presentation.screen.UserInformationScreen
import org.designsystem.project.theme.AppTheme
@Composable
@Preview
fun App() {

    LaunchedEffect(Unit) {
        Sentry.init { options ->

            options.dsn = "https://2a221d31c48fd91b735b7890485c97de@o4512324232216576.ingest.us.sentry.io/4512324239622144"
            options.environment = "development"

        }
        //Sentry.captureException(Exception("Error de prueba desde la App"))
    }

    AppTheme {
        ExchangeScreen()
    }
}
