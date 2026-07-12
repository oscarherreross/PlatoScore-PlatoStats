package oscar.platoscore.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val esDama: Boolean = false
)
