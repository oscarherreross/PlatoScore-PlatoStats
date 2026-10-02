package oscar.platostats.models

import androidx.room.Embedded
import androidx.room.Relation

/** Una [Tirada] con sus series, para leerla completa de una vez. */
data class TiradaConSeries(
    @Embedded val tirada: Tirada,
    @Relation(
        parentColumn = "id",
        entityColumn = "tiradaId"
    )
    val series: List<Serie>
) {
    /** Platos máximos posibles: 25 por serie. */
    val platosPosibles: Int get() = series.size * Serie.PLATOS_POR_SERIE

    /** Total de platos rotos en toda la tirada. */
    val platosRotos: Int get() = series.sumOf { it.platosRotos }

    /** Total de platos rotos al primer tiro; null si ninguna serie lo registró. */
    val platosPrimerTiro: Int?
        get() {
            val conDato = series.mapNotNull { it.platosPrimerTiro }
            return if (conDato.isEmpty()) null else conDato.sum()
        }

    /** Porcentaje de aciertos (0..100); 0 si no hay series. */
    val porcentaje: Float
        get() = if (platosPosibles == 0) 0f else platosRotos * 100f / platosPosibles
}
