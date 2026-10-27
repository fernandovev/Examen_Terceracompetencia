package org.ucb.appp1.monitoring

import android.content.Context
import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.Insert
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

// Cada fila representa un valor observado y su fecha.
@Entity(tableName = "monitoring_history")
data class MonitoringChange(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val value: Boolean,
    val timestamp: Long
)

@Dao
interface MonitoringDao {

    @Insert
    suspend fun insert(change: MonitoringChange)

    @Query("SELECT * FROM monitoring_history ORDER BY id DESC")
    fun observeHistory(): Flow<List<MonitoringChange>>

    @Query("SELECT * FROM monitoring_history ORDER BY id DESC LIMIT 1")
    suspend fun getLatest(): MonitoringChange?
}

@Database(
    entities = [MonitoringChange::class],
    version = 1,
    exportSchema = true
)
abstract class MonitoringDatabase : RoomDatabase() {

    abstract fun monitoringDao(): MonitoringDao

    companion object {

        @Volatile
        private var instance: MonitoringDatabase? = null

        fun getInstance(context: Context): MonitoringDatabase {
            return instance ?: synchronized(this) {
                instance ?: createDatabase(context).also {
                    instance = it
                }
            }
        }

        private fun createDatabase(context: Context): MonitoringDatabase {
            val appContext = context.applicationContext
            val databaseFile = appContext.getDatabasePath("monitoring.db")

            return Room.databaseBuilder<MonitoringDatabase>(
                context = appContext,
                name = databaseFile.absolutePath
            )
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
        }
    }
}