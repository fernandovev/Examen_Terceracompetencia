package org.ucb.appp1.exchange.data.repository

import kotlinx.coroutines.flow.Flow
import org.ucb.appp1.exchange.data.datasource.RealTimeDataBase
import org.ucb.appp1.exchange.data.dao.DollarDao
import org.ucb.appp1.exchange.data.entity.DollarEntity
import org.ucb.appp1.exchange.domain.repository.ExchangeRepository

class ExchangeRepositoryImpl(
    val realTimeDataBase: RealTimeDataBase,
    val dollarDao: DollarDao
): ExchangeRepository {

    override suspend fun observe(): Flow<String?> {
        return realTimeDataBase.observeMessage()
    }

    override fun getDollarsFlow(): Flow<List<DollarEntity>> {
        return dollarDao.getDollarList()
    }

    override suspend fun saveDollar(dollar: DollarEntity) {
        dollarDao.insertDollar(dollar)
    }
}
