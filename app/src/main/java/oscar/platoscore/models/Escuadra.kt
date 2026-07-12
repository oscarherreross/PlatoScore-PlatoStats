package oscar.platoscore.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "escuadras",
    foreignKeys = [
        ForeignKey(
            entity = Tirada::class,
            parentColumns = ["id"],
            childColumns = ["tiradaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tiradaId")]
)
data class Escuadra(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tiradaId: Int,
    val numeroEscuadra: Int = 1
)
