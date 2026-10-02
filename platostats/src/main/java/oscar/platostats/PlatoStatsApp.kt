package oscar.platostats

import android.app.Application
import oscar.plato.core.PlatoApp
import oscar.platostats.ui.activities.PersonalMainActivity

/**
 * Aplicación PlatoStats: el tirador registra sus tiradas y consulta sus
 * estadísticas. Indica a las pantallas comunes de :core (inicio de sesión,
 * pantalla de carga) a dónde ir y qué marca mostrar.
 */
class PlatoStatsApp : Application(), PlatoApp {
    override val pantallaPrincipal = PersonalMainActivity::class.java
    override val logo = R.drawable.ic_logo
    override val lema = R.string.splash_tagline
}
