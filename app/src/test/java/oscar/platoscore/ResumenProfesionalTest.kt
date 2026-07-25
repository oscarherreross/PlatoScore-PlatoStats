package oscar.platoscore

import org.junit.Assert.assertEquals
import org.junit.Test
import oscar.platoscore.models.ResumenProfesionalCalc
import oscar.platoscore.models.Tirada
import oscar.platoscore.models.TiradaConContadores
import oscar.platoscore.models.Tirador
import oscar.platoscore.models.TiradorConTirada

class ResumenProfesionalTest {

    private val tiradaA = Tirada(
        id = 1,
        nombre = "A",
        precioLocal = 20f,
        precioGeneral = 30f,
        precioJunior = 15f
    )

    @Test
    fun `cuenta tiradas, escuadras y tiradores desde los contadores`() {
        val tiradas = listOf(
            TiradaConContadores(tiradaA, numEscuadras = 2, numTiradores = 3),
            TiradaConContadores(Tirada(id = 2, nombre = "B"), numEscuadras = 1, numTiradores = 0)
        )
        val resumen = ResumenProfesionalCalc.calcular(tiradas, emptyList())

        assertEquals(2, resumen.numTiradas)
        assertEquals(3, resumen.totalEscuadras)
        assertEquals(3, resumen.totalTiradores)
    }

    @Test
    fun `recaudacion suma el precio de cada tirador con los precios de su tirada`() {
        val tiradores = listOf(
            TiradorConTirada(Tirador(id = 1, esLocal = true), tiradaA),   // 20 (local)
            TiradorConTirada(Tirador(id = 2), tiradaA),                    // 30 (general)
            TiradorConTirada(Tirador(id = 3, esJunior = true), tiradaA)    // 15 (junior)
        )
        val resumen = ResumenProfesionalCalc.calcular(emptyList(), tiradores)

        assertEquals(65f, resumen.recaudacion, 0.001f)
    }

    @Test
    fun `sin datos, todo a cero`() {
        val resumen = ResumenProfesionalCalc.calcular(emptyList(), emptyList())
        assertEquals(0, resumen.numTiradas)
        assertEquals(0, resumen.totalEscuadras)
        assertEquals(0, resumen.totalTiradores)
        assertEquals(0f, resumen.recaudacion, 0.001f)
    }
}
