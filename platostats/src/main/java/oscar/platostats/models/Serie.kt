package oscar.platostats.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Una serie de 25 platos dentro de una [Tirada]. */
@Entity(
    tableName = "series",
    foreignKeys = [
        ForeignKey(
            entity = Tirada::class,
            parentColumns = ["id"],
            childColumns = ["tiradaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tiradaId")]
)
data class Serie(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tiradaId: Int = 0,
    val numeroSerie: Int = 0,
    /** Puesto de tiro en el que se disputa esta serie. */
    val puesto: Int = 0,
    /** Platos rotos en la serie (obligatorio), de 0 a 25. */
    val platosRotos: Int = 0,
    /** Platos rotos al primer tiro (opcional), de 0 a 25. */
    val platosPrimerTiro: Int? = null
) {
    companion object {
        const val PLATOS_POR_SERIE = 25
    }
}
