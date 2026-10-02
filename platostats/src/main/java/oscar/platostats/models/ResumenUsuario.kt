package oscar.platostats.models

/** Datos agregados del tirador para el menú de perfil. */
data class ResumenUsuario(
    /** Total de platos a los que ha disparado (todos los lanzados). */
    val totalPlatos: Int,
    /** Total de tiros (cartuchos) realizados. */
    val totalTiros: Int,
    /** % de aciertos solo en entrenamientos; null si no hay datos. */
    val porcentajeEntrenamiento: Float?,
    /** % de aciertos solo en competiciones; null si no hay datos. */
    val porcentajeCompeticion: Float?,
    /** % de aciertos contando todo; null si no hay datos. */
    val porcentajeGeneral: Float?
)

object PerfilUsuario {

    /**
     * Calcula el resumen del tirador a partir de todas sus tiradas.
     *
     * "Tiros realizados" aplica la regla de 2 cartuchos por plato: siempre se
     * dispara el primer tiro y un segundo tiro salvo que el plato se rompa al
     * primero. En las series sin dato de primer tiro se asume que los platos
     * rotos lo fueron al primer disparo.
     */
    fun calcular(tiradas: List<TiradaConSeries>): ResumenUsuario {
        val totalPlatos = tiradas.sumOf { it.platosPosibles }
        val totalTiros = tiradas.flatMap { it.series }.sumOf { serie ->
            val primerTiro = serie.platosPrimerTiro ?: serie.platosRotos
            2 * Serie.PLATOS_POR_SERIE - primerTiro
        }
        return ResumenUsuario(
            totalPlatos = totalPlatos,
            totalTiros = totalTiros,
            porcentajeEntrenamiento = porcentaje(
                tiradas.filter { it.tirada.tipo == Tirada.TIPO_ENTRENAMIENTO }
            ),
            porcentajeCompeticion = porcentaje(
                tiradas.filter { it.tirada.tipo == Tirada.TIPO_COMPETICION }
            ),
            porcentajeGeneral = porcentaje(tiradas)
        )
    }

    private fun porcentaje(tiradas: List<TiradaConSeries>): Float? {
        val posibles = tiradas.sumOf { it.platosPosibles }
        if (posibles == 0) return null
        return tiradas.sumOf { it.platosRotos } * 100f / posibles
    }
}
