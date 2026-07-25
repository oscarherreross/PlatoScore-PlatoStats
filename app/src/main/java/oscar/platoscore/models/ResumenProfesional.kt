package oscar.platoscore.models

/** Datos agregados del profesional para el menú de perfil. */
data class ResumenProfesional(
    val numTiradas: Int,
    val totalEscuadras: Int,
    val totalTiradores: Int,
    /** Suma de la inscripción de todos los tiradores, con los precios vigentes. */
    val recaudacion: Float
)

object ResumenProfesionalCalc {

    fun calcular(
        tiradas: List<TiradaConContadores>,
        tiradores: List<TiradorConTirada>
    ): ResumenProfesional = ResumenProfesional(
        numTiradas = tiradas.size,
        totalEscuadras = tiradas.sumOf { it.numEscuadras },
        totalTiradores = tiradas.sumOf { it.numTiradores },
        recaudacion = tiradores.sumOf { it.tirada.precioPara(it.tirador).toDouble() }.toFloat()
    )
}
