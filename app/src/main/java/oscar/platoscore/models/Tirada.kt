package oscar.platoscore.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tiradas")
data class Tirada(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String = "",
    val fecha: String = "",
    val precioLocal: Float = 0f,
    val precioGeneral: Float = 0f,
    val precioJunior: Float = 0f,
    val precioSenior: Float = 0f,
    val precioDama: Float = 0f
)
