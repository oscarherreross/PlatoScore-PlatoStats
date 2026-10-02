package oscar.platostats

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import oscar.plato.core.models.FiltroTiradas
import oscar.plato.core.utils.Fechas
import oscar.platostats.models.SeriePersonal
import oscar.platostats.models.TiradaPersonal
import oscar.platostats.models.TiradaPersonalConSeries
import oscar.platostats.models.acepta

class FiltroTiradasTest {

    private fun personal(tipo: String, maquina: String, fechaHora: Long) =
        TiradaPersonalConSeries(
            tirada = TiradaPersonal(
                userId = "u", lugar = "L", tipo = tipo, maquina = maquina, fechaHora = fechaHora
            ),
            series = listOf(SeriePersonal(platosRotos = 20))
        )

    @Test
    fun `filtro vacio acepta cualquier tirada personal`() {
        val t = personal(TiradaPersonal.TIPO_ENTRENAMIENTO, TiradaPersonal.MAQUINA_ROBOT, 1000L)
        assertTrue(FiltroTiradas().acepta(t))
    }

    @Test
    fun `filtro por tipo y maquina`() {
        val t = personal(TiradaPersonal.TIPO_COMPETICION, TiradaPersonal.MAQUINA_TRAP, 1000L)
        assertTrue(FiltroTiradas(tipo = TiradaPersonal.TIPO_COMPETICION).acepta(t))
        assertFalse(FiltroTiradas(tipo = TiradaPersonal.TIPO_ENTRENAMIENTO).acepta(t))
        assertTrue(FiltroTiradas(maquina = TiradaPersonal.MAQUINA_TRAP).acepta(t))
        assertFalse(FiltroTiradas(maquina = TiradaPersonal.MAQUINA_ROBOT).acepta(t))
    }

    @Test
    fun `filtro por fechas`() {
        val medioDia = Fechas.inicioDelDiaMillis(2026, 7, 12) + 12 * 3600 * 1000L
        val t = personal(TiradaPersonal.TIPO_ENTRENAMIENTO, TiradaPersonal.MAQUINA_ROBOT, medioDia)
        val dentro = FiltroTiradas(
            desde = Fechas.inicioDelDiaMillis(2026, 7, 12),
            hasta = Fechas.finDelDiaMillis(2026, 7, 12)
        )
        assertTrue(dentro.acepta(t))
        assertFalse(FiltroTiradas(desde = Fechas.inicioDelDiaMillis(2026, 7, 13)).acepta(t))
    }
}
