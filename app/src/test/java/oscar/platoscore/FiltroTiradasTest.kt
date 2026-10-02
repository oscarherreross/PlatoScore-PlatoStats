package oscar.platoscore

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import oscar.plato.core.models.FiltroTiradas
import oscar.plato.core.utils.Fechas
import oscar.platoscore.models.Tirada
import oscar.platoscore.models.TiradaConContadores
import oscar.platoscore.models.acepta

class FiltroTiradasTest {

    private val tirada = TiradaConContadores(
        Tirada(nombre = "A", fecha = "2026-07-12"), numEscuadras = 0, numTiradores = 0
    )

    @Test
    fun `filtro vacio acepta cualquier tirada`() {
        assertTrue(FiltroTiradas().acepta(tirada))
    }

    @Test
    fun `filtro por fechas compara la fecha ISO`() {
        val dentro = FiltroTiradas(
            desde = Fechas.inicioDelDiaMillis(2026, 7, 10),
            hasta = Fechas.finDelDiaMillis(2026, 7, 15)
        )
        assertTrue(dentro.acepta(tirada))
        assertFalse(FiltroTiradas(desde = Fechas.inicioDelDiaMillis(2026, 7, 13)).acepta(tirada))
        assertFalse(FiltroTiradas(hasta = Fechas.finDelDiaMillis(2026, 7, 11)).acepta(tirada))
    }

    @Test
    fun `tipo y maquina no aplican a las tiradas de PlatoScore`() {
        assertTrue(FiltroTiradas(tipo = "competicion", maquina = "trap").acepta(tirada))
    }
}
