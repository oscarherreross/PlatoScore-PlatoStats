package oscar.platoscore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import oscar.platoscore.models.EstadisticasPersonales
import oscar.platoscore.models.SeriePersonal
import oscar.platoscore.models.TiradaPersonal
import oscar.platoscore.models.TiradaPersonalConSeries

class EstadisticasPersonalesTest {

    private fun tirada(
        id: Int,
        fechaHora: Long,
        series: List<SeriePersonal>
    ) = TiradaPersonalConSeries(
        tirada = TiradaPersonal(id = id, userId = "u", lugar = "L", fechaHora = fechaHora),
        series = series
    )

    private fun serie(rotos: Int, primer: Int? = null, puesto: Int = 1) =
        SeriePersonal(puesto = puesto, platosRotos = rotos, platosPrimerTiro = primer)

    @Test
    fun `totales y porcentaje de una tirada`() {
        val t = tirada(1, 1000, listOf(serie(20), serie(25), serie(22)))
        assertEquals(75, t.platosPosibles)
        assertEquals(67, t.platosRotos)
        assertEquals(67 * 100f / 75, t.porcentaje, 0.001f)
    }

    @Test
    fun `primer tiro es null si ninguna serie lo registra`() {
        val t = tirada(1, 1000, listOf(serie(20), serie(25)))
        assertNull(t.platosPrimerTiro)
    }

    @Test
    fun `primer tiro suma solo las series que lo tienen`() {
        val t = tirada(1, 1000, listOf(serie(20, 15), serie(25, 20), serie(22)))
        assertEquals(35, t.platosPrimerTiro)
    }

    @Test
    fun `sin tiradas el resumen no tiene datos`() {
        val resumen = EstadisticasPersonales.calcular(emptyList())
        assertFalse(resumen.hayDatos)
        assertEquals(0, resumen.numTiradas)
        assertNull(resumen.mediaPorcentajePrimerTiro)
    }

    @Test
    fun `las tiradas sin series se ignoran`() {
        val resumen = EstadisticasPersonales.calcular(
            listOf(tirada(1, 1000, emptyList()))
        )
        assertFalse(resumen.hayDatos)
    }

    @Test
    fun `los puntos se ordenan cronologicamente aunque lleguen desordenados`() {
        val reciente = tirada(1, 5000, listOf(serie(25)))
        val antigua = tirada(2, 1000, listOf(serie(10)))
        val resumen = EstadisticasPersonales.calcular(listOf(reciente, antigua))

        assertEquals(listOf(1000L, 5000L), resumen.puntos.map { it.fechaHora })
        assertEquals(2, resumen.numTiradas)
    }

    @Test
    fun `media por puesto agrupa las series de todas las tiradas y ordena por puesto`() {
        // Tirada 1: puesto 2 -> 25/25, puesto 1 -> 20/25
        // Tirada 2: puesto 1 -> 15/25
        val t1 = tirada(1, 1000, listOf(serie(25, puesto = 2), serie(20, puesto = 1)))
        val t2 = tirada(2, 2000, listOf(serie(15, puesto = 1)))
        val resumen = EstadisticasPersonales.calcular(listOf(t1, t2))

        // Ordenado por número de puesto.
        assertEquals(listOf(1, 2), resumen.mediaPorPuesto.map { it.puesto })

        val puesto1 = resumen.mediaPorPuesto.first { it.puesto == 1 }
        // (20 + 15) / (2 * 25) = 70 %
        assertEquals(70f, puesto1.porcentaje, 0.001f)
        assertEquals(2, puesto1.numSeries)

        val puesto2 = resumen.mediaPorPuesto.first { it.puesto == 2 }
        assertEquals(100f, puesto2.porcentaje, 0.001f)
        assertEquals(1, puesto2.numSeries)
    }

    @Test
    fun `medias mejor y primer tiro`() {
        // Tirada A: 1 serie, 20/25 = 80%, primer 10/25 = 40%
        // Tirada B: 1 serie, 25/25 = 100%, sin primer tiro
        val a = tirada(1, 1000, listOf(serie(20, 10)))
        val b = tirada(2, 2000, listOf(serie(25)))
        val resumen = EstadisticasPersonales.calcular(listOf(a, b))

        assertTrue(resumen.hayDatos)
        assertEquals(22.5f, resumen.mediaPlatos, 0.001f)
        assertEquals(90f, resumen.mediaPorcentaje, 0.001f)
        assertEquals(100f, resumen.mejorPorcentaje, 0.001f)
        // Solo A tiene primer tiro (40%); la media de primer tiro es 40.
        assertEquals(40f, resumen.mediaPorcentajePrimerTiro!!, 0.001f)
    }
}
