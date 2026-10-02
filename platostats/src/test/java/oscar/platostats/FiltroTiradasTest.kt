package oscar.platostats

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import oscar.plato.core.models.FiltroTiradas
import oscar.plato.core.utils.Fechas
import oscar.platostats.models.Serie
import oscar.platostats.models.Tirada
import oscar.platostats.models.TiradaConSeries
import oscar.platostats.models.acepta

class FiltroTiradasTest {

    private fun tirada(tipo: String, maquina: String, fechaHora: Long) =
        TiradaConSeries(
            tirada = Tirada(
                userId = "u", lugar = "L", tipo = tipo, maquina = maquina, fechaHora = fechaHora
            ),
            series = listOf(Serie(platosRotos = 20))
        )

    @Test
    fun `filtro vacio acepta cualquier tirada`() {
        val t = tirada(Tirada.TIPO_ENTRENAMIENTO, Tirada.MAQUINA_ROBOT, 1000L)
        assertTrue(FiltroTiradas().acepta(t))
    }

    @Test
    fun `filtro por tipo y maquina`() {
        val t = tirada(Tirada.TIPO_COMPETICION, Tirada.MAQUINA_TRAP, 1000L)
        assertTrue(FiltroTiradas(tipo = Tirada.TIPO_COMPETICION).acepta(t))
        assertFalse(FiltroTiradas(tipo = Tirada.TIPO_ENTRENAMIENTO).acepta(t))
        assertTrue(FiltroTiradas(maquina = Tirada.MAQUINA_TRAP).acepta(t))
        assertFalse(FiltroTiradas(maquina = Tirada.MAQUINA_ROBOT).acepta(t))
    }

    @Test
    fun `filtro por fechas`() {
        val medioDia = Fechas.inicioDelDiaMillis(2026, 7, 12) + 12 * 3600 * 1000L
        val t = tirada(Tirada.TIPO_ENTRENAMIENTO, Tirada.MAQUINA_ROBOT, medioDia)
        val dentro = FiltroTiradas(
            desde = Fechas.inicioDelDiaMillis(2026, 7, 12),
            hasta = Fechas.finDelDiaMillis(2026, 7, 12)
        )
        assertTrue(dentro.acepta(t))
        assertFalse(FiltroTiradas(desde = Fechas.inicioDelDiaMillis(2026, 7, 13)).acepta(t))
    }
}
