package oscar.platoscore.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Las fechas se persisten en formato ISO (yyyy-MM-dd) para que el orden
 * alfabético en SQL coincida con el cronológico, y se muestran al usuario
 * en formato dd/MM/yyyy.
 */
object Fechas {

    private const val FORMATO_ISO = "yyyy-MM-dd"
    private const val FORMATO_VISUAL = "dd/MM/yyyy"

    fun hoyIso(): String =
        SimpleDateFormat(FORMATO_ISO, Locale.getDefault()).format(Date())

    fun mostrar(fechaIso: String): String = try {
        val fecha = SimpleDateFormat(FORMATO_ISO, Locale.getDefault()).parse(fechaIso)
        if (fecha != null) {
            SimpleDateFormat(FORMATO_VISUAL, Locale.getDefault()).format(fecha)
        } else {
            fechaIso
        }
    } catch (e: Exception) {
        fechaIso
    }
}
