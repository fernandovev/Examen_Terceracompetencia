package org.ucb.appp1.exchange.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import org.ucb.appp1.exchange.data.entity.DollarEntity

@Dao
interface DollarDao {
    @Query("SELECT * FROM DollarEntity")
    fun getDollarList(): Flow<List<DollarEntity>>

    @Insert
    suspend fun insertDollar(dollar: DollarEntity)

    @Query("DELETE FROM DollarEntity WHERE id = :id")
    suspend fun deleteById(id: String)
}
