package oscar.platoscore

import org.junit.Assert.assertEquals
import org.junit.Test
import oscar.platoscore.models.Clasificacion
import oscar.platoscore.models.Tirada
import oscar.platoscore.models.Tirador

class ClasificacionTest {

    private val tirada = Tirada(
        id = 1,
        precioLocal = 20f,
        precioGeneral = 30f,
        precioJunior = 15f,
        precioSenior = 25f,
        precioDama = 18f
    )

    private fun tirador(
        id: Int,
        nombre: String,
        platos: Int,
        ordenDesempate: Int = 0,
        esLocal: Boolean = false,
        esJunior: Boolean = false
    ) = Tirador(
        id = id,
        escuadraId = 1,
        nombreApellidos = nombre,
        platosRotos = platos,
        esLocal = esLocal,
        esJunior = esJunior,
        ordenDesempate = ordenDesempate
    )

    @Test
    fun `sin empates, ordena por platos y asigna posiciones consecutivas`() {
        val resultados = Clasificacion.generar(
            listOf(
                tirador(1, "Ana", platos = 18),
                tirador(2, "Berto", platos = 22),
                tirador(3, "Carlos", platos = 20)
            ),
            tirada
        )

        assertEquals(listOf("Berto", "Carlos", "Ana"), resultados.map { it.tirador.nombreApellidos })
        assertEquals(listOf(1, 2, 3), resultados.map { it.posicion })
        assertEquals(listOf(false, false, false), resultados.map { it.empatado })
    }

    @Test
    fun `empate sin resolver comparte posicion y la siguiente se salta`() {
        val resultados = Clasificacion.generar(
            listOf(
                tirador(1, "Ana", platos = 20),
                tirador(2, "Berto", platos = 22),
                tirador(3, "Carlos", platos = 20),
                tirador(4, "David", platos = 15)
            ),
            tirada
        )

        // Berto 1º; Ana y Carlos empatados a 20 comparten el 2º (alfabético entre sí); David 4º
        assertEquals(listOf("Berto", "Ana", "Carlos", "David"), resultados.map { it.tirador.nombreApellidos })
        assertEquals(listOf(1, 2, 2, 4), resultados.map { it.posicion })
        assertEquals(listOf(false, true, true, false), resultados.map { it.empatado })
    }

    @Test
    fun `empate resuelto ordena por ordenDesempate con posiciones consecutivas`() {
        val resultados = Clasificacion.generar(
            listOf(
                tirador(1, "Ana", platos = 20, ordenDesempate = 2),
                tirador(2, "Berto", platos = 22),
                tirador(3, "Carlos", platos = 20, ordenDesempate = 1),
                tirador(4, "David", platos = 15)
            ),
            tirada
        )

        // Carlos ganó el desempate a Ana pese a ir detrás alfabéticamente
        assertEquals(listOf("Berto", "Carlos", "Ana", "David"), resultados.map { it.tirador.nombreApellidos })
        assertEquals(listOf(1, 2, 3, 4), resultados.map { it.posicion })
        assertEquals(listOf(false, false, false, false), resultados.map { it.empatado })
    }

    @Test
    fun `empate parcialmente resuelto mezcla orden manual y alfabetico`() {
        val resultados = Clasificacion.generar(
            listOf(
                tirador(1, "Zacarias", platos = 20, ordenDesempate = 1),
                tirador(2, "Ana", platos = 20),
                tirador(3, "Berto", platos = 20)
            ),
            tirada
        )

        // Zacarías resuelto primero; Ana y Berto siguen empatados compartiendo el 2º
        assertEquals(listOf("Zacarias", "Ana", "Berto"), resultados.map { it.tirador.nombreApellidos })
        assertEquals(listOf(1, 2, 2), resultados.map { it.posicion })
        assertEquals(listOf(false, true, true), resultados.map { it.empatado })
    }

    @Test
    fun `el precio se calcula con los precios vigentes de la tirada`() {
        val resultados = Clasificacion.generar(
            listOf(
                tirador(1, "Ana", platos = 20, esLocal = true, esJunior = true),
                tirador(2, "Berto", platos = 18)
            ),
            tirada
        )

        // Ana: mínimo entre Local (20) y Junior (15); Berto: General (30)
        assertEquals(15f, resultados[0].precio)
        assertEquals(30f, resultados[1].precio)
    }
}
