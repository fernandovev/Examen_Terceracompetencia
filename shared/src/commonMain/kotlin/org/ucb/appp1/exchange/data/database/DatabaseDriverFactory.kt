package org.ucb.appp1.exchange.data.database

import androidx.room3.RoomDatabaseConstructor

expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase

    fun create(context: Any): AppDatabase
}

@androidx.room3.Database(
    entities = [org.ucb.appp1.exchange.data.entity.DollarEntity::class],
    version = 1
)
abstract class AppDatabase : androidx.room3.RoomDatabase() {
    abstract fun dollarDao(): org.ucb.appp1.exchange.data.dao.DollarDao
}