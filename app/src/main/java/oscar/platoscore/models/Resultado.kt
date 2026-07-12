package oscar.platoscore.models

data class Resultado(
    val tirador: Tirador,
    val clasificacionLocal: Int? = null,
    val clasificacionGeneral: Int? = null,
    val clasificacionJunior: Int? = null,
    val clasificacionSenior: Int? = null,
    val clasificacionDama: Int? = null,
    val totalPuntos: Float = 0f
)
