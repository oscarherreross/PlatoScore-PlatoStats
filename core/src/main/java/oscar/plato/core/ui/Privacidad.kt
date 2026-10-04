package oscar.plato.core.ui

import android.app.Activity
import android.content.Intent

/** Abre la política de privacidad de la app en curso, que va dentro de la propia app. */
fun Activity.abrirPoliticaPrivacidad() {
    startActivity(Intent(this, PoliticaPrivacidadActivity::class.java))
}
