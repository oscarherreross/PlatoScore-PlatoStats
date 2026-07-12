package oscar.platoscore.models

data class Resultado(
    val tirador: Tirador,
    val precio: Float = 0f,
    /** Posición en la clasificación; los empatados sin resolver la comparten. */
    val posicion: Int = 0,
    val empatado: Boolean = false
)
