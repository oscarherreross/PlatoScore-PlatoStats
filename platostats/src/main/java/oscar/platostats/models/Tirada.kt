package oscar.platostats.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Registro que hace el tirador de una tirada en la que ha
 * participado. Los platos de cada serie se guardan en [Serie].
 */
@Entity(tableName = "tiradas")
data class Tirada(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    /** UID de Firebase del tirador dueño del registro. */
    val userId: String = "",
    val lugar: String = "",
    /** Fecha y hora de la tirada en epoch millis. */
    val fechaHora: Long = 0,
    val numeroEscuadra: Int = 0,
    /** Clasificación de la tirada: [TIPO_COMPETICION] o [TIPO_ENTRENAMIENTO]. */
    val tipo: String = TIPO_ENTRENAMIENTO,
    /** Máquina de lanzamiento: [MAQUINA_ROBOT], [MAQUINA_TRAP] u [MAQUINA_OLIMPICO]. */
    val maquina: String = MAQUINA_ROBOT,
    val notas: String = ""
) {
    companion object {
        const val TIPO_COMPETICION = "competicion"
        const val TIPO_ENTRENAMIENTO = "entrenamiento"

        const val MAQUINA_ROBOT = "robot"
        const val MAQUINA_TRAP = "trap"
        const val MAQUINA_OLIMPICO = "olimpico"
    }
}
