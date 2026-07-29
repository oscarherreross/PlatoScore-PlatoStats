package oscar.platoscore

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import oscar.platoscore.models.FiltroTiradas
import oscar.platoscore.models.SeriePersonal
import oscar.platoscore.models.Tirada
import oscar.platoscore.models.TiradaConContadores
import oscar.platoscore.models.TiradaPersonal
import oscar.platoscore.models.TiradaPersonalConSeries
import oscar.platoscore.utils.Fechas

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
    fun `filtro por tipo y maquina en personal`() {
        val t = personal(TiradaPersonal.TIPO_COMPETICION, TiradaPersonal.MAQUINA_TRAP, 1000L)
        assertTrue(FiltroTiradas(tipo = TiradaPersonal.TIPO_COMPETICION).acepta(t))
        assertFalse(FiltroTiradas(tipo = TiradaPersonal.TIPO_ENTRENAMIENTO).acepta(t))
        assertTrue(FiltroTiradas(maquina = TiradaPersonal.MAQUINA_TRAP).acepta(t))
        assertFalse(FiltroTiradas(maquina = TiradaPersonal.MAQUINA_ROBOT).acepta(t))
    }

    @Test
    fun `filtro por fechas en personal`() {
        val medioDia = Fechas.inicioDelDiaMillis(2026, 7, 12) + 12 * 3600 * 1000L
        val t = personal(TiradaPersonal.TIPO_ENTRENAMIENTO, TiradaPersonal.MAQUINA_ROBOT, medioDia)
        val dentro = FiltroTiradas(
            desde = Fechas.inicioDelDiaMillis(2026, 7, 12),
            hasta = Fechas.finDelDiaMillis(2026, 7, 12)
        )
        assertTrue(dentro.acepta(t))
        assertFalse(FiltroTiradas(desde = Fechas.inicioDelDiaMillis(2026, 7, 13)).acepta(t))
    }

    @Test
    fun `filtro por fechas en profesional compara la fecha ISO`() {
        val t = TiradaConContadores(
            Tirada(nombre = "A", fecha = "2026-07-12"), numEscuadras = 0, numTiradores = 0
        )
        val dentro = FiltroTiradas(
            desde = Fechas.inicioDelDiaMillis(2026, 7, 10),
            hasta = Fechas.finDelDiaMillis(2026, 7, 15)
        )
        assertTrue(dentro.acepta(t))
        assertFalse(FiltroTiradas(desde = Fechas.inicioDelDiaMillis(2026, 7, 13)).acepta(t))
        // Tipo y máquina no aplican al profesional: se ignoran.
        assertTrue(
            FiltroTiradas(
                tipo = TiradaPersonal.TIPO_COMPETICION,
                maquina = TiradaPersonal.MAQUINA_TRAP
            ).acepta(t)
        )
    }

    @Test
    fun `activo refleja si hay algun filtro`() {
        assertFalse(FiltroTiradas().activo)
        assertTrue(FiltroTiradas(tipo = TiradaPersonal.TIPO_COMPETICION).activo)
        assertTrue(FiltroTiradas(desde = 1000L).activo)
    }
}
