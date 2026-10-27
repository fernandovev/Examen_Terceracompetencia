package org.ucb.appp1.exchange.data.datasource

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.remoteconfig.remoteConfig

class RemoteConfigDataSource {
    suspend fun getDollarPrice(): Double {
        val remoteConfig = Firebase.remoteConfig

        // Durante las pruebas, permite descargar sin esperar.
        remoteConfig.settings {
            minimumFetchIntervalInSeconds = 0
        }

        remoteConfig.fetchAndActivate()

        val price = remoteConfig.getValue("dollar_price").asDouble()
        println("Remote Config dollar_price: $price")

        return price
    }
}