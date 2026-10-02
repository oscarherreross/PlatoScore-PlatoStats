package oscar.plato.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import oscar.plato.core.models.FiltroTiradas

/** Lo común del filtro. Qué tiradas lo cumplen se prueba en cada app. */
class FiltroTiradasTest {

    @Test
    fun `activo refleja si hay algun filtro`() {
        assertFalse(FiltroTiradas().activo)
        assertTrue(FiltroTiradas(tipo = "competicion").activo)
        assertTrue(FiltroTiradas(maquina = "trap").activo)
        assertTrue(FiltroTiradas(desde = 1000L).activo)
        assertTrue(FiltroTiradas(hasta = 1000L).activo)
    }
}
