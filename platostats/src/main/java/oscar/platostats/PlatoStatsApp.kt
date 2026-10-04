package oscar.platostats

import android.app.Application
import oscar.plato.core.PlatoApp
import oscar.platostats.database.PlatoStatsDatabase
import oscar.platostats.ui.activities.MainActivity

/**
 * Aplicación PlatoStats: el tirador registra sus tiradas y consulta sus
 * estadísticas. Indica a las pantallas comunes de :core (inicio de sesión,
 * pantalla de carga, cuenta) a dónde ir, qué marca mostrar y qué datos borrar con
 * la cuenta.
 */
class PlatoStatsApp : Application(), PlatoApp {
    override val pantallaPrincipal = MainActivity::class.java
    override val logo = R.drawable.ic_logo
    override val lema = R.string.splash_tagline

    override val carpetaLegal = "platostats"

    override suspend fun borrarDatosDe(uid: String) {
        PlatoStatsDatabase.getDatabase(this).tiradaDao().deleteTiradasDe(uid)
    }
}
