package oscar.platostats.models

/** Un punto de la evolución del tirador: una tirada en el tiempo. */
data class PuntoEvolucion(
    val fechaHora: Long,
    val platosRotos: Int,
    val platosPosibles: Int,
    val porcentaje: Float,
    val porcentajePrimerTiro: Float?
)

/** Media de aciertos en un puesto de tiro concreto, sobre todas las series. */
data class AciertoPorPuesto(
    val puesto: Int,
    val porcentaje: Float,
    val numSeries: Int
)

/** Media de aciertos con una máquina concreta, sobre todas sus tiradas. */
data class AciertoPorMaquina(
    val maquina: String,
    val porcentaje: Float,
    val numTiradas: Int
)

/** Resumen de la evolución de un tirador a lo largo de sus tiradas. */
data class ResumenEstadisticas(
    val puntos: List<PuntoEvolucion>,
    val numTiradas: Int,
    val mediaPlatos: Float,
    val mejorPorcentaje: Float,
    val mediaPorcentaje: Float,
    val mediaPorcentajePrimerTiro: Float?,
    val mediaPorPuesto: List<AciertoPorPuesto>,
    val mediaPorMaquina: List<AciertoPorMaquina>
) {
    val hayDatos: Boolean get() = puntos.isNotEmpty()
}

object EstadisticasPersonales {

    private val VACIO = ResumenEstadisticas(emptyList(), 0, 0f, 0f, 0f, null, emptyList(), emptyList())

    /** Orden fijo de las máquinas para mostrarlas siempre igual. */
    private val ORDEN_MAQUINAS = listOf(
        TiradaPersonal.MAQUINA_ROBOT,
        TiradaPersonal.MAQUINA_TRAP,
        TiradaPersonal.MAQUINA_OLIMPICO
    )

    /**
     * Calcula el resumen a partir de las tiradas del tirador. Los puntos de
     * evolución quedan ordenados cronológicamente (de la más antigua a la más
     * reciente) para poder pintar la curva de progreso.
     */
    fun calcular(tiradas: List<TiradaPersonalConSeries>): ResumenEstadisticas {
        val validas = tiradas.filter { it.series.isNotEmpty() }
            .sortedBy { it.tirada.fechaHora }

        if (validas.isEmpty()) return VACIO

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
                else porcentajesPrimerTiro.average().toFloat(),
            mediaPorPuesto = mediaPorPuesto(validas),
            mediaPorMaquina = mediaPorMaquina(validas)
        )
    }

    /** Agrupa todas las series por puesto de tiro y calcula el % de aciertos de cada uno. */
    private fun mediaPorPuesto(tiradas: List<TiradaPersonalConSeries>): List<AciertoPorPuesto> {
        return tiradas.flatMap { it.series }
            .groupBy { it.puesto }
            .toSortedMap()
            .map { (puesto, series) ->
                val posibles = series.size * SeriePersonal.PLATOS_POR_SERIE
                val rotos = series.sumOf { it.platosRotos }
                AciertoPorPuesto(
                    puesto = puesto,
                    porcentaje = if (posibles == 0) 0f else rotos * 100f / posibles,
                    numSeries = series.size
                )
            }
    }

    /** Agrupa las tiradas por máquina y calcula el % de aciertos de cada una. */
    private fun mediaPorMaquina(tiradas: List<TiradaPersonalConSeries>): List<AciertoPorMaquina> {
        return tiradas.groupBy { it.tirada.maquina }
            .map { (maquina, lista) ->
                val posibles = lista.sumOf { it.platosPosibles }
                val rotos = lista.sumOf { it.platosRotos }
                AciertoPorMaquina(
                    maquina = maquina,
                    porcentaje = if (posibles == 0) 0f else rotos * 100f / posibles,
                    numTiradas = lista.size
                )
            }
            .sortedBy { ORDEN_MAQUINAS.indexOf(it.maquina).let { i -> if (i < 0) Int.MAX_VALUE else i } }
    }
}
