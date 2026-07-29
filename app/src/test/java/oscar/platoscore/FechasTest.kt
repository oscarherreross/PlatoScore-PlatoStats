package oscar.platoscore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import oscar.platoscore.utils.Fechas

class FechasTest {

    @Test
    fun `hoyIso devuelve el formato yyyy-MM-dd`() {
        assertTrue(Fechas.hoyIso().matches(Regex("""\d{4}-\d{2}-\d{2}""")))
    }

    @Test
    fun `mostrar convierte ISO a dd MM yyyy`() {
        assertEquals("21/02/2026", Fechas.mostrar("2026-02-21"))
    }

    @Test
    fun `mostrar devuelve la entrada tal cual si no es ISO valida`() {
        assertEquals("sin fecha", Fechas.mostrar("sin fecha"))
        assertEquals("", Fechas.mostrar(""))
    }

    @Test
    fun `aIso rellena con ceros`() {
        assertEquals("2026-02-01", Fechas.aIso(2026, 2, 1))
        assertEquals("2026-12-31", Fechas.aIso(2026, 12, 31))
    }

    @Test
    fun `partesIso descompone una fecha valida`() {
        assertEquals(Triple(2026, 7, 12), Fechas.partesIso("2026-07-12"))
    }

    @Test
    fun `aIso y partesIso son inversas`() {
        val (anio, mes, dia) = Fechas.partesIso("2025-01-09")
        assertEquals("2025-01-09", Fechas.aIso(anio, mes, dia))
    }

    @Test
    fun `inicio y fin del dia mantienen el mismo dia y su orden`() {
        val inicio = Fechas.inicioDelDiaMillis(2026, 7, 12)
        val fin = Fechas.finDelDiaMillis(2026, 7, 12)
        assertTrue(inicio < fin)
        assertEquals("2026-07-12", Fechas.isoDeMillis(inicio))
        assertEquals("2026-07-12", Fechas.isoDeMillis(fin))
        assertEquals(Triple(2026, 7, 12), Fechas.partesDeMillis(inicio))
    }

    @Test
    fun `un instante del dia cae dentro del rango inicio-fin`() {
        val inicio = Fechas.inicioDelDiaMillis(2026, 7, 12)
        val fin = Fechas.finDelDiaMillis(2026, 7, 12)
        // Mediodía de ese mismo día.
        val (a, m, d) = Fechas.partesDeMillis(inicio)
        assertEquals(2026, a); assertEquals(7, m); assertEquals(12, d)
        val medioDia = inicio + 12 * 60 * 60 * 1000L
        assertTrue(medioDia in inicio..fin)
    }
}
