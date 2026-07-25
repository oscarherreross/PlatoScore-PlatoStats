package oscar.platoscore.models

import androidx.room.Embedded

/**
 * Un tirador junto a la tirada a la que pertenece. Permite calcular la
 * recaudación reutilizando [Tirada.precioPara] en lugar de replicar esa
 * lógica en SQL.
 */
data class TiradorConTirada(
    @Embedded val tirador: Tirador,
    @Embedded(prefix = "t_") val tirada: Tirada
)
