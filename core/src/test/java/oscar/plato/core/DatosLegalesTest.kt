package oscar.plato.core

import org.junit.Assert.assertEquals
import org.junit.Test
import oscar.plato.core.models.DatosLegales
import java.io.StringReader

class DatosLegalesTest {

    private val pagina = "<p>{{RESPONSABLE}}</p><a href=\"mailto:{{CONTACTO}}\">{{CONTACTO}}</a>"

    private fun leer(propiedades: String) = DatosLegales.leer(StringReader(propiedades))

    @Test
    fun `rellena las marcas con los datos del archivo`() {
        val datos = leer("# comentario\nresponsable=Club de Tiro Peña\ncontacto = hola@ejemplo.test\n")
        assertEquals(
            "<p>Club de Tiro Peña</p><a href=\"mailto:hola@ejemplo.test\">hola@ejemplo.test</a>",
            datos.rellenar(pagina, "pendiente")
        )
    }

    @Test
    fun `los marcadores de la plantilla cuentan como datos sin rellenar`() {
        val datos = leer("responsable=NOMBRE_DEL_RESPONSABLE\ncontacto=CORREO_DE_CONTACTO\n")
        assertEquals(
            "<p>(pendiente)</p><a href=\"mailto:(pendiente)\">(pendiente)</a>",
            datos.rellenar(pagina, "(pendiente)")
        )
    }

    @Test
    fun `un dato que falta tambien se muestra como pendiente`() {
        assertEquals("<p>X</p>", leer("contacto=a@b.test\n").rellenar("<p>{{RESPONSABLE}}</p>", "X"))
    }

    @Test
    fun `los caracteres especiales no rompen la pagina`() {
        val datos = leer("responsable=Pérez & Hijos <S.L.>\ncontacto=a@b.test\n")
        assertEquals("<p>Pérez &amp; Hijos &lt;S.L.&gt;</p>", datos.rellenar("<p>{{RESPONSABLE}}</p>", ""))
    }
}
