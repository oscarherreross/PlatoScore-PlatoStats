package oscar.platoscore.models

import androidx.room.Embedded

/** Escuadra junto a su número de tiradores, para mostrarlo en la lista. */
data class EscuadraConContadores(
    @Embedded val escuadra: Escuadra,
    val numTiradores: Int
)
