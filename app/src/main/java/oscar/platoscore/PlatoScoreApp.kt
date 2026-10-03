package oscar.platoscore

import android.app.Application
import oscar.plato.core.PlatoApp
import oscar.platoscore.database.PlatoScoreDatabase
import oscar.platoscore.ui.activities.MainActivity

/**
 * Aplicación PlatoScore: gestión de tiradas para organizadores. Indica a las
 * pantallas comunes de :core (inicio de sesión, pantalla de carga, cuenta) a dónde
 * ir, qué marca mostrar y qué datos borrar con la cuenta.
 */
class PlatoScoreApp : Application(), PlatoApp {
    override val pantallaPrincipal = MainActivity::class.java
    override val logo = R.drawable.ic_logo
    override val lema = R.string.splash_tagline

    override val urlPrivacidad: String
        get() = getString(R.string.url_privacidad)

    override suspend fun borrarDatosDe(uid: String) {
        PlatoScoreDatabase.getDatabase(this).tiradaDao().deleteDeUsuario(uid)
    }
}
