package org.ucb.appp1.exchange.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ucb.appp1.exchange.data.datasource.FirestoreDataSource // ✅ Nuevo
import org.ucb.appp1.exchange.data.datasource.RemoteConfigDataSource
import org.ucb.appp1.exchange.data.entity.DollarEntity
import org.ucb.appp1.exchange.domain.repository.ExchangeRepository

class ExchangeViewModel(
    private val repository: ExchangeRepository,
    private val remoteConfig: RemoteConfigDataSource,
    private val firestore: FirestoreDataSource
) : ViewModel() {

    val dollarList: StateFlow<List<DollarEntity>> = repository.getDollarsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSaveDollarClick() {
        viewModelScope.launch {
            try {
                val precioNube = remoteConfig.getDollarPrice()

                val nuevoDolar = DollarEntity(
                    id = kotlin.random.Random.nextInt(1, 999999).toString(),
                    name = "Dólar Oficial",
                    price = precioNube
                )

                repository.saveDollar(nuevoDolar)

                firestore.saveDollarToCloud(nuevoDolar)

            } catch (e: Exception) {
                println("Error al guardar: ${e.message}")
            }
        }
    }
}