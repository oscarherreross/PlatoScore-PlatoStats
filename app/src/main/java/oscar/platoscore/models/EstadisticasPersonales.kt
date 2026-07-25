package oscar.platoscore.models

/** Un punto de la evolución del tirador: una tirada en el tiempo. */
data class PuntoEvolucion(
    val fechaHora: Long,
    val platosRotos: Int,
    val platosPosibles: Int,
    val porcentaje: Float,
    val porcentajePrimerTiro: Float?
)

/** Resumen de la evolución de un tirador a lo largo de sus tiradas. */
data class ResumenEstadisticas(
    val puntos: List<PuntoEvolucion>,
    val numTiradas: Int,
    val mediaPlatos: Float,
    val mejorPorcentaje: Float,
    val mediaPorcentaje: Float,
    val mediaPorcentajePrimerTiro: Float?
) {
    val hayDatos: Boolean get() = puntos.isNotEmpty()
}

object EstadisticasPersonales {

    /**
     * Calcula el resumen a partir de las tiradas del tirador. Los puntos de
     * evolución quedan ordenados cronológicamente (de la más antigua a la más
     * reciente) para poder pintar la curva de progreso.
     */
    fun calcular(tiradas: List<TiradaPersonalConSeries>): ResumenEstadisticas {
        val validas = tiradas.filter { it.series.isNotEmpty() }
            .sortedBy { it.tirada.fechaHora }

        if (validas.isEmpty()) {
            return ResumenEstadisticas(emptyList(), 0, 0f, 0f, 0f, null)
        }

        val puntos = validas.map { t ->
            val porcentajePrimerTiro = t.platosPrimerTiro?.let { primer ->
                if (t.platosPosibles == 0) null else primer * 100f / t.platosPosibles
            }
            PuntoEvolucion(
                fechaHora = t.tirada.fechaHora,
                platosRotos = t.platosRotos,
                platosPosibles = t.platosPosibles,
                porcentaje = t.porcentaje,
                porcentajePrimerTiro = porcentajePrimerTiro
            )
        }

        val porcentajesPrimerTiro = puntos.mapNotNull { it.porcentajePrimerTiro }

        return ResumenEstadisticas(
            puntos = puntos,
            numTiradas = puntos.size,
            mediaPlatos = puntos.map { it.platosRotos }.average().toFloat(),
            mejorPorcentaje = puntos.maxOf { it.porcentaje },
            mediaPorcentaje = puntos.map { it.porcentaje }.average().toFloat(),
            mediaPorcentajePrimerTiro =
                if (porcentajesPrimerTiro.isEmpty()) null
                else porcentajesPrimerTiro.average().toFloat()
        )
    }
}
