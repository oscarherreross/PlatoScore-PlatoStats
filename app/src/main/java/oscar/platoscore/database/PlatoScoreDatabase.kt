package oscar.platoscore.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import oscar.platoscore.models.Escuadra
import oscar.platoscore.models.Tirada
import oscar.platoscore.models.Tirador

@Database(
    entities = [Tirada::class, Escuadra::class, Tirador::class],
    version = 1,
    exportSchema = false
)
abstract class PlatoScoreDatabase : RoomDatabase() {

    abstract fun tiradaDao(): TiradaDao
    abstract fun escuadraDao(): EscuadraDao
    abstract fun tiradorDao(): TiradorDao

    companion object {
        @Volatile private var INSTANCE: PlatoScoreDatabase? = null

        fun getDatabase(context: Context): PlatoScoreDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    PlatoScoreDatabase::class.java,
                    "platoscore_database"
                ).fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
