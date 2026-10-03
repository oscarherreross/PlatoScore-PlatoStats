package oscar.platoscore.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "tiradores",
    foreignKeys = [
        ForeignKey(
            entity = Escuadra::class,
            parentColumns = ["id"],
            childColumns = ["escuadraId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("escuadraId")]
)
data class Tirador(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val escuadraId: Int = 0,
    val nombreApellidos: String = "",
    val dni: String = "",
    val numeroLicencia: String = "",
    val platosRotos: Int = 0,
    val esLocal: Boolean = false,
    val esJunior: Boolean = false,
    val esSenior: Boolean = false,
    val esDama: Boolean = false,
    /**
     * Resultado del desempate manual entre los tiradores de la tirada que
     * empatan a platos rotos: 1 = ganador del desempate, 2 = segundo, etc.
     * 0 = desempate sin resolver.
     */
    val ordenDesempate: Int = 0
) : Parcelable
