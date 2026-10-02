package oscar.platostats

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import oscar.platostats.models.PerfilUsuario
import oscar.platostats.models.SeriePersonal
import oscar.platostats.models.TiradaPersonal
import oscar.platostats.models.TiradaPersonalConSeries

class PerfilUsuarioTest {

    private fun tirada(
        tipo: String,
        series: List<SeriePersonal>
    ) = TiradaPersonalConSeries(
        tirada = TiradaPersonal(userId = "u", lugar = "L", tipo = tipo),
        series = series
    )

    private fun serie(rotos: Int, primer: Int? = null) =
        SeriePersonal(platosRotos = rotos, platosPrimerTiro = primer)

    @Test
    fun `total de platos suma 25 por serie`() {
        val r = PerfilUsuario.calcular(
            listOf(tirada(TiradaPersonal.TIPO_ENTRENAMIENTO, listOf(serie(20), serie(18))))
        )
        assertEquals(50, r.totalPlatos)
    }

    @Test
    fun `total de tiros con dato de primer tiro`() {
        // 1 serie: 20 rotos, 15 al primer tiro
        // tiros = 25 (primeros) + (25 - 15) segundos = 35
        val r = PerfilUsuario.calcular(
            listOf(tirada(TiradaPersonal.TIPO_ENTRENAMIENTO, listOf(serie(20, primer = 15))))
        )
        assertEquals(35, r.totalTiros)
    }

    @Test
    fun `total de tiros sin dato de primer tiro asume roturas al primer disparo`() {
        // 1 serie: 20 rotos, sin primer tiro -> se asume primer = 20
        // tiros = 25 + (25 - 20) = 30
        val r = PerfilUsuario.calcular(
            listOf(tirada(TiradaPersonal.TIPO_ENTRENAMIENTO, listOf(serie(20))))
        )
        assertEquals(30, r.totalTiros)
    }

    @Test
    fun `porcentajes por tipo y general`() {
        // Entrenamiento: 20/25 = 80 %
        // Competición: 22/25 y 24/25 = 46/50 = 92 %
        // General: (20 + 46) / 75 = 88 %
        val r = PerfilUsuario.calcular(
            listOf(
                tirada(TiradaPersonal.TIPO_ENTRENAMIENTO, listOf(serie(20))),
                tirada(TiradaPersonal.TIPO_COMPETICION, listOf(serie(22), serie(24)))
            )
        )
        assertEquals(80f, r.porcentajeEntrenamiento!!, 0.001f)
        assertEquals(92f, r.porcentajeCompeticion!!, 0.001f)
        assertEquals(88f, r.porcentajeGeneral!!, 0.001f)
    }

    @Test
    fun `porcentaje null cuando no hay tiradas de ese tipo`() {
        val r = PerfilUsuario.calcular(
            listOf(tirada(TiradaPersonal.TIPO_ENTRENAMIENTO, listOf(serie(20))))
        )
        assertNull(r.porcentajeCompeticion)
        assertNull(PerfilUsuario.calcular(emptyList()).porcentajeGeneral)
    }
}
