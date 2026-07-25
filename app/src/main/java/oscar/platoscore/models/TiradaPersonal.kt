package oscar.platoscore.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Registro que hace un tirador (rol personal) de una tirada en la que ha
 * participado. Los platos de cada serie se guardan en [SeriePersonal].
 */
@Entity(tableName = "tiradas_personales")
data class TiradaPersonal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    /** UID de Firebase del tirador dueño del registro. */
    val userId: String = "",
    val lugar: String = "",
    /** Fecha y hora de la tirada en epoch millis. */
    val fechaHora: Long = 0,
    val numeroEscuadra: Int = 0,
    val puestoInicial: Int = 0,
    val notas: String = ""
)
