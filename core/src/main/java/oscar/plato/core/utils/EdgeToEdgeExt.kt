package oscar.plato.core.utils

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge

/**
 * Activa edge-to-edge en pantallas cuyo Toolbar de color (colorPrimary) se
 * extiende bajo la barra de estado. Fuerza los iconos de la barra de estado a
 * claros, igual que el contenido (blanco) del toolbar; la barra de navegación
 * se deja en automático porque queda sobre el contenido normal de la pantalla.
 */
fun ComponentActivity.enableEdgeToEdgeConToolbar() {
    enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
}
