package oscar.platoscore.models

import androidx.room.Embedded
import androidx.room.Relation

data class EscuadraConTiradores(
    @Embedded val escuadra: Escuadra,
    @Relation(
        parentColumn = "id",
        entityColumn = "escuadraId"
    )
    val tiradores: List<Tirador>
)
