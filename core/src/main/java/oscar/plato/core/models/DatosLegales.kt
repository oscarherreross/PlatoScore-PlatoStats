package oscar.plato.core.models

import java.io.Reader
import java.util.Properties

/**
 * Quién responde del tratamiento de los datos y cómo contactar con él. Son los dos
 * datos que completan las páginas de legal/ (política de privacidad y eliminación de
 * cuenta) y se escriben una sola vez, en legal/datos-legales.properties.
 */
class DatosLegales(private val responsable: String, private val contacto: String) {

    /**
     * Devuelve [html] con las marcas {{RESPONSABLE}} y {{CONTACTO}} sustituidas. Un
     * dato que sigue sin rellenar se muestra como [pendiente].
     */
    fun rellenar(html: String, pendiente: String): String = html
        .replace("{{RESPONSABLE}}", enHtml(responsable.ifEmpty { pendiente }))
        .replace("{{CONTACTO}}", enHtml(contacto.ifEmpty { pendiente }))

    private fun enHtml(texto: String): String =
        texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    companion object {

        /** Los marcadores de la plantilla (NOMBRE_DEL_RESPONSABLE…) son solo mayúsculas y guiones bajos. */
        private val MARCADOR = Regex("[A-Z_]+")

        /** Lee los datos de un archivo .properties; los que conservan su marcador cuentan como vacíos. */
        fun leer(archivo: Reader): DatosLegales {
            val propiedades = Properties().apply { load(archivo) }
            fun dato(clave: String): String =
                propiedades.getProperty(clave).orEmpty().trim().takeUnless { MARCADOR.matches(it) }.orEmpty()
            return DatosLegales(responsable = dato("responsable"), contacto = dato("contacto"))
        }
    }
}
