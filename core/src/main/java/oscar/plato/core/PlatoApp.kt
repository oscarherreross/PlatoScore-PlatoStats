package oscar.plato.core

import android.app.Activity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Lo que cada aplicación (PlatoScore, PlatoStats) aporta a las pantallas comunes
 * de :core. Lo implementa la clase Application de cada app: así el inicio de
 * sesión, la pantalla de carga y la cuenta son compartidos, pero cada app entra
 * en su propia pantalla principal y muestra su propia marca.
 */
interface PlatoApp {

    /** Pantalla a la que se entra tras iniciar sesión (o directamente si ya hay sesión). */
    val pantallaPrincipal: Class<out Activity>

    /** Logotipo de la pantalla de carga (sin fondo: se dibuja sobre el naranja). */
    @get:DrawableRes
    val logo: Int

    /** Lema que aparece bajo el nombre en la pantalla de carga. */
    @get:StringRes
    val lema: Int
}

/** Configuración de la app en curso. */
val Activity.platoApp: PlatoApp
    get() = application as PlatoApp

/** Nombre visible de la app (la etiqueta declarada en su manifest). */
val Activity.nombreApp: CharSequence
    get() = applicationInfo.loadLabel(packageManager)
