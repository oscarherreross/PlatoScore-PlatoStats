package oscar.platostats.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import oscar.platostats.models.Serie
import oscar.platostats.models.Tirada

/**
 * Base de datos local de PlatoStats: las tiradas del tirador y sus series,
 * aisladas por usuario (UID de Firebase). Es independiente de la de PlatoScore:
 * cada app guarda sus datos en su propio almacenamiento.
 *
 * Nace en la versión 1 con el esquema que estas tablas tenían en la v6 de
 * PlatoScore (allí tiradas_personales/series_personales; aquí, tiradas/series).
 * Igual que en PlatoScore, los cambios futuros deben ir con migraciones reales
 * (sin fallbackToDestructiveMigration) y con el esquema exportado en
 * platostats/schemas.
 */
@Database(
    entities = [Tirada::class, Serie::class],
    version = 1,
    exportSchema = true
)
abstract class PlatoStatsDatabase : RoomDatabase() {

    abstract fun tiradaDao(): TiradaDao

    companion object {
        @Volatile private var INSTANCE: PlatoStatsDatabase? = null

        fun getDatabase(context: Context): PlatoStatsDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    PlatoStatsDatabase::class.java,
                    "platostats_database"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
