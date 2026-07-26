package oscar.platoscore.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import oscar.platoscore.models.Escuadra
import oscar.platoscore.models.SeriePersonal
import oscar.platoscore.models.Tirada
import oscar.platoscore.models.TiradaPersonal
import oscar.platoscore.models.Tirador

@Database(
    entities = [
        Tirada::class, Escuadra::class, Tirador::class,
        TiradaPersonal::class, SeriePersonal::class
    ],
    version = 6,
    exportSchema = true
)
abstract class PlatoScoreDatabase : RoomDatabase() {

    abstract fun tiradaDao(): TiradaDao
    abstract fun escuadraDao(): EscuadraDao
    abstract fun tiradorDao(): TiradorDao
    abstract fun tiradaPersonalDao(): TiradaPersonalDao

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

        /**
         * v3 -> v4: soporte de cuentas y del modo personal.
         *  - tiradas.userId aísla las tiradas de cada profesional. Las
         *    tiradas anteriores a las cuentas quedan con userId '' (sin dueño).
         *  - Nuevas tablas tiradas_personales y series_personales para los
         *    registros del rol personal.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tiradas ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `tiradas_personales` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`userId` TEXT NOT NULL, " +
                            "`lugar` TEXT NOT NULL, " +
                            "`fechaHora` INTEGER NOT NULL, " +
                            "`numeroEscuadra` INTEGER NOT NULL, " +
                            "`puestoInicial` INTEGER NOT NULL, " +
                            "`notas` TEXT NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `series_personales` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`tiradaPersonalId` INTEGER NOT NULL, " +
                            "`numeroSerie` INTEGER NOT NULL, " +
                            "`platosRotos` INTEGER NOT NULL, " +
                            "`platosPrimerTiro` INTEGER, " +
                            "FOREIGN KEY(`tiradaPersonalId`) REFERENCES `tiradas_personales`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_series_personales_tiradaPersonalId` " +
                            "ON `series_personales` (`tiradaPersonalId`)"
                )
            }
        }

        /**
         * v4 -> v5: modo personal.
         *  - tiradas_personales.tipo (competición/entrenamiento) y se elimina
         *    tiradas_personales.puestoInicial (el puesto pasa a cada serie).
         *  - series_personales.puesto: el puesto de tiro es ahora por serie.
         *
         * Para quitar una columna (SQLite no soporta DROP COLUMN en versiones
         * antiguas) se recrean ambas tablas. Se vuelca antes a tablas de
         * respaldo SIN claves foráneas: así, al borrar las tablas originales,
         * el ON DELETE CASCADE no puede arrastrar las series. Los registros
         * previos toman tipo 'entrenamiento' y puesto 1 por defecto.
         */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Respaldos planos (CREATE TABLE AS SELECT no copia las FK).
                db.execSQL(
                    "CREATE TABLE `_tp_backup` AS SELECT " +
                            "id, userId, lugar, fechaHora, numeroEscuadra, notas " +
                            "FROM tiradas_personales"
                )
                db.execSQL(
                    "CREATE TABLE `_sp_backup` AS SELECT " +
                            "id, tiradaPersonalId, numeroSerie, platosRotos, platosPrimerTiro " +
                            "FROM series_personales"
                )

                // 2. Fuera las tablas originales (los respaldos no tienen FK).
                db.execSQL("DROP TABLE series_personales")
                db.execSQL("DROP TABLE tiradas_personales")

                // 3. Tablas nuevas con el esquema v5.
                db.execSQL(
                    "CREATE TABLE `tiradas_personales` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`userId` TEXT NOT NULL, " +
                            "`lugar` TEXT NOT NULL, " +
                            "`fechaHora` INTEGER NOT NULL, " +
                            "`numeroEscuadra` INTEGER NOT NULL, " +
                            "`tipo` TEXT NOT NULL, " +
                            "`notas` TEXT NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE `series_personales` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`tiradaPersonalId` INTEGER NOT NULL, " +
                            "`numeroSerie` INTEGER NOT NULL, " +
                            "`puesto` INTEGER NOT NULL, " +
                            "`platosRotos` INTEGER NOT NULL, " +
                            "`platosPrimerTiro` INTEGER, " +
                            "FOREIGN KEY(`tiradaPersonalId`) REFERENCES `tiradas_personales`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_series_personales_tiradaPersonalId` " +
                            "ON `series_personales` (`tiradaPersonalId`)"
                )

                // 4. Recuperar los datos con los valores por defecto nuevos.
                db.execSQL(
                    "INSERT INTO tiradas_personales " +
                            "(id, userId, lugar, fechaHora, numeroEscuadra, tipo, notas) " +
                            "SELECT id, userId, lugar, fechaHora, numeroEscuadra, 'entrenamiento', notas " +
                            "FROM _tp_backup"
                )
                db.execSQL(
                    "INSERT INTO series_personales " +
                            "(id, tiradaPersonalId, numeroSerie, puesto, platosRotos, platosPrimerTiro) " +
                            "SELECT id, tiradaPersonalId, numeroSerie, 1, platosRotos, platosPrimerTiro " +
                            "FROM _sp_backup"
                )

                // 5. Limpiar respaldos.
                db.execSQL("DROP TABLE _tp_backup")
                db.execSQL("DROP TABLE _sp_backup")
            }
        }

        /**
         * v5 -> v6: máquina de lanzamiento por tirada personal
         * (robot/trap/olímpico). Los registros previos toman 'robot'.
         */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE tiradas_personales ADD COLUMN maquina TEXT NOT NULL DEFAULT 'robot'"
                )
            }
        }

        fun getDatabase(context: Context): PlatoScoreDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    PlatoScoreDatabase::class.java,
                    "platoscore_database"
                ).addMigrations(
                    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
