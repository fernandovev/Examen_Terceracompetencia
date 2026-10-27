package org.ucb.appp1.exchange.data.database

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {

    actual override fun initialize(): AppDatabase {
        throw UnsupportedOperationException("Usa create(context) en Android")
    }

    actual fun create(context: Any): AppDatabase {
        val androidContext = context as Context
        return Room.databaseBuilder<AppDatabase>(
            context = androidContext,
            name = "app_database.db"
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}