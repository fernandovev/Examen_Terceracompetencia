package org.ucb.appp1.exchange.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ucb.appp1.exchange.data.entity.DollarEntity

interface ExchangeRepository {
    suspend fun observe(): Flow<String?>

    fun getDollarsFlow(): Flow<List<DollarEntity>>

    suspend fun saveDollar(dollar: DollarEntity)
}
