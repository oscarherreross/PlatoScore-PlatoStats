package oscar.platoscore.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import oscar.platoscore.models.Escuadra
import oscar.platoscore.models.Tirada
import oscar.platoscore.models.Tirador

@Database(
    entities = [Tirada::class, Escuadra::class, Tirador::class],
    version = 3,
    exportSchema = true
)
abstract class PlatoScoreDatabase : RoomDatabase() {

    abstract fun tiradaDao(): TiradaDao
    abstract fun escuadraDao(): EscuadraDao
    abstract fun tiradorDao(): TiradorDao

    companion object {
        @Volatile private var INSTANCE: PlatoScoreDatabase? = null

        /**
         * v1 -> v2:
         *  - Las fechas de las tiradas pasan de dd/MM/yyyy a formato ISO
         *    (yyyy-MM-dd) para que ORDER BY fecha ordene cronológicamente.
         *  - Se elimina la columna tiradores.precio: el precio se calcula
         *    siempre a partir de los precios vigentes de la tirada.
         *  - Se añade el índice sobre tiradores.escuadraId que exige la
         *    clave foránea.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "UPDATE tiradas SET fecha = substr(fecha, 7, 4) || '-' || " +
                            "substr(fecha, 4, 2) || '-' || substr(fecha, 1, 2) " +
                            "WHERE fecha LIKE '__/__/____'"
                )
                db.execSQL(
                    "CREATE TABLE tiradores_new (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`escuadraId` INTEGER NOT NULL, " +
                            "`nombreApellidos` TEXT NOT NULL, " +
                            "`dni` TEXT NOT NULL, " +
                            "`numeroLicencia` TEXT NOT NULL, " +
                            "`platosRotos` INTEGER NOT NULL, " +
                            "`esLocal` INTEGER NOT NULL, " +
                            "`esJunior` INTEGER NOT NULL, " +
                            "`esSenior` INTEGER NOT NULL, " +
                            "`esDama` INTEGER NOT NULL, " +
                            "FOREIGN KEY(`escuadraId`) REFERENCES `escuadras`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "INSERT INTO tiradores_new (id, escuadraId, nombreApellidos, dni, " +
                            "numeroLicencia, platosRotos, esLocal, esJunior, esSenior, esDama) " +
                            "SELECT id, escuadraId, nombreApellidos, dni, numeroLicencia, " +
                            "platosRotos, esLocal, esJunior, esSenior, esDama FROM tiradores"
                )
                db.execSQL("DROP TABLE tiradores")
                db.execSQL("ALTER TABLE tiradores_new RENAME TO tiradores")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_tiradores_escuadraId` " +
                            "ON `tiradores` (`escuadraId`)"
                )
            }
        }

        /**
         * v2 -> v3: columna tiradores.ordenDesempate para registrar el
         * resultado del desempate manual entre empatados a platos rotos.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE tiradores ADD COLUMN ordenDesempate INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getDatabase(context: Context): PlatoScoreDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    PlatoScoreDatabase::class.java,
                    "platoscore_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
