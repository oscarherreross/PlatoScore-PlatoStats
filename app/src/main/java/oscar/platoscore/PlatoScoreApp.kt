package oscar.platoscore

import android.app.Application
import oscar.plato.core.PlatoApp
import oscar.platoscore.ui.activities.MainActivity

/**
 * Aplicación PlatoScore: gestión de tiradas para organizadores. Indica a las
 * pantallas comunes de :core (inicio de sesión, pantalla de carga) a dónde ir y
 * qué marca mostrar.
 */
class PlatoScoreApp : Application(), PlatoApp {
    override val pantallaPrincipal = MainActivity::class.java
    override val logo = R.drawable.ic_logo
    override val lema = R.string.splash_tagline
}
