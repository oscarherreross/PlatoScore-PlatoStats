package oscar.platoscore.models

/**
 * Genera la clasificación de una categoría a partir de sus tiradores:
 * ordena por platos rotos (descendente), aplica el orden de desempate
 * manual si está resuelto y asigna posiciones.
 *
 * Los empatados sin desempate resuelto comparten posición y la siguiente
 * se salta (1, 2, 2, 4...), quedando ordenados alfabéticamente entre sí.
 * Los empates resueltos ([Tirador.ordenDesempate] > 0) reciben posiciones
 * consecutivas según ese orden.
 */
object Clasificacion {

    fun generar(tiradores: List<Tirador>, tirada: Tirada): List<Resultado> {
        val ordenados = tiradores.sortedWith(
            compareByDescending<Tirador> { it.platosRotos }
                .thenBy { if (it.ordenDesempate > 0) it.ordenDesempate else Int.MAX_VALUE }
                .thenBy { it.nombreApellidos.lowercase() }
        )

        val resultados = mutableListOf<Resultado>()
        var i = 0
        while (i < ordenados.size) {
            // Tamaño del grupo de empate sin resolver que empieza en i:
            // tiradores consecutivos con los mismos platos y sin orden asignado
            var tamanoGrupo = 1
            if (ordenados[i].ordenDesempate == 0) {
                while (i + tamanoGrupo < ordenados.size &&
                    ordenados[i + tamanoGrupo].platosRotos == ordenados[i].platosRotos &&
                    ordenados[i + tamanoGrupo].ordenDesempate == 0
                ) {
                    tamanoGrupo++
                }
            }

            val empatados = tamanoGrupo > 1
            for (j in 0 until tamanoGrupo) {
                val tirador = ordenados[i + j]
                resultados.add(
                    Resultado(
                        tirador = tirador,
                        precio = tirada.precioPara(tirador),
                        posicion = i + 1,
                        empatado = empatados
                    )
                )
            }
            i += tamanoGrupo
        }
        return resultados
    }
}
