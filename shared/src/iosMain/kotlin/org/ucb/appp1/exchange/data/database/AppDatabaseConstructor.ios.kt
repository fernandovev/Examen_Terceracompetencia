package org.ucb.appp1.exchange.data.database

import androidx.room3.Room
import androidx.room3.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    actual override fun initialize(): AppDatabase {
        return Room.databaseBuilder<AppDatabase>(name = "app_database.db")
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}
