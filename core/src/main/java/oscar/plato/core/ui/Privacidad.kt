package oscar.plato.core.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import oscar.plato.core.R
import oscar.plato.core.platoApp

/** Abre en el navegador la política de privacidad de la app en curso. */
fun Activity.abrirPoliticaPrivacidad() {
    val url = platoApp.urlPrivacidad
    val abierta = url.isNotBlank() && try {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
    if (!abierta) {
        Toast.makeText(this, R.string.toast_privacidad_no_disponible, Toast.LENGTH_LONG).show()
    }
}
