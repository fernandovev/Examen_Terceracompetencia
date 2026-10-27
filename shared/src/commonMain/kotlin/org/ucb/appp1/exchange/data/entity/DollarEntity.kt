package org.ucb.appp1.exchange.data.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class DollarEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double
)

