package oscar.plato.core.utils

import java.text.SimpleDateFormat
import java.util.Calendar
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
    private const val FORMATO_FECHA_HORA = "dd/MM/yyyy HH:mm"

    fun hoyIso(): String =
        SimpleDateFormat(FORMATO_ISO, Locale.getDefault()).format(Date())

    fun ahoraMillis(): Long = System.currentTimeMillis()

    /** Fecha y hora (epoch millis) mostradas como dd/MM/yyyy HH:mm. */
    fun mostrarFechaHora(millis: Long): String =
        SimpleDateFormat(FORMATO_FECHA_HORA, Locale.getDefault()).format(Date(millis))

    /** Fecha (epoch millis) mostrada como dd/MM/yyyy. */
    fun mostrarFecha(millis: Long): String =
        SimpleDateFormat(FORMATO_VISUAL, Locale.getDefault()).format(Date(millis))

    /** Fecha (epoch millis) en formato ISO yyyy-MM-dd (para comparar con Tirada.fecha). */
    fun isoDeMillis(millis: Long): String =
        SimpleDateFormat(FORMATO_ISO, Locale.getDefault()).format(Date(millis))

    /** Descompone una fecha (epoch millis) en (año, mes 1-12, día). */
    fun partesDeMillis(millis: Long): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return Triple(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /** Epoch millis del inicio del día (00:00:00.000). Mes en 1-12. */
    fun inicioDelDiaMillis(anio: Int, mes: Int, dia: Int): Long =
        diaMillis(anio, mes, dia, finDelDia = false)

    /** Epoch millis del final del día (23:59:59.999). Mes en 1-12. */
    fun finDelDiaMillis(anio: Int, mes: Int, dia: Int): Long =
        diaMillis(anio, mes, dia, finDelDia = true)

    private fun diaMillis(anio: Int, mes: Int, dia: Int, finDelDia: Boolean): Long {
        val cal = Calendar.getInstance()
        cal.set(anio, mes - 1, dia)
        if (finDelDia) {
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
        } else {
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

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

    fun aIso(anio: Int, mes: Int, dia: Int): String =
        String.format(Locale.US, "%04d-%02d-%02d", anio, mes, dia)

    /** Descompone una fecha ISO en (año, mes 1-12, día); si no es válida, hoy. */
    fun partesIso(fechaIso: String): Triple<Int, Int, Int> {
        val trozos = fechaIso.split("-")
        if (trozos.size == 3) {
            val anio = trozos[0].toIntOrNull()
            val mes = trozos[1].toIntOrNull()
            val dia = trozos[2].toIntOrNull()
            if (anio != null && mes != null && dia != null) {
                return Triple(anio, mes, dia)
            }
        }
        val hoy = Calendar.getInstance()
        return Triple(
            hoy.get(Calendar.YEAR),
            hoy.get(Calendar.MONTH) + 1,
            hoy.get(Calendar.DAY_OF_MONTH)
        )
    }
}
