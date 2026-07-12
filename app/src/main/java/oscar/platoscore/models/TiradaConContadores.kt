package oscar.platoscore.models

import androidx.room.Embedded

/** Tirada junto a sus totales, para mostrarlos en la lista. */
data class TiradaConContadores(
    @Embedded val tirada: Tirada,
    val numEscuadras: Int,
    val numTiradores: Int
)
