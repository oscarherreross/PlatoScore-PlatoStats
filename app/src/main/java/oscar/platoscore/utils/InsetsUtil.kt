package oscar.platoscore.utils

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

/**
 * Utilidades para el modo edge-to-edge: la app se dibuja de borde a borde con
 * las barras del sistema transparentes, y estas funciones añaden el hueco de
 * esas barras como padding/margen a las vistas del borde, para que el contenido
 * no quede tapado. El valor original del padding/margen se conserva y se le suma
 * el inset, de modo que la llamada es idempotente aunque el sistema la repita.
 */
object InsetsUtil {

    private fun barras(insets: WindowInsetsCompat) =
        insets.getInsets(WindowInsetsCompat.Type.systemBars())

    /** Suma el inset superior (barra de estado) al padding-top. */
    fun padTop(view: View) {
        val base = view.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            v.updatePadding(top = base + barras(insets).top)
            insets
        }
    }

    /** Suma el inset inferior (barra de navegación) al padding-bottom. */
    fun padBottom(view: View) {
        val base = view.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            v.updatePadding(bottom = base + barras(insets).bottom)
            insets
        }
    }

    /** Suma los insets superior e inferior al padding vertical. */
    fun padVertical(view: View) {
        val top = view.paddingTop
        val bottom = view.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val b = barras(insets)
            v.updatePadding(top = top + b.top, bottom = bottom + b.bottom)
            insets
        }
    }

    /** Suma el inset inferior al margen inferior (para FABs y botones flotantes). */
    fun addMarginBottom(view: View) {
        val base = (view.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = base + barras(insets).bottom
            }
            insets
        }
    }
}
