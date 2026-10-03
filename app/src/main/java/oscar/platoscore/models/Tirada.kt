package oscar.platoscore.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tiradas")
data class Tirada(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    /** UID de Firebase del profesional dueño de la tirada; aísla los datos por usuario. */
    val userId: String = "",
    val nombre: String = "",
    /** Fecha en formato ISO (yyyy-MM-dd); ver [oscar.plato.core.utils.Fechas]. */
    val fecha: String = "",
    val precioLocal: Float = 0f,
    val precioGeneral: Float = 0f,
    val precioJunior: Float = 0f,
    val precioSenior: Float = 0f,
    val precioDama: Float = 0f
) : Parcelable {

    /**
     * Precio de inscripción para un tirador según sus categorías, con los
     * precios vigentes de esta tirada: el mínimo de las categorías marcadas,
     * o el precio General si no tiene ninguna.
     */
    fun precioPara(tirador: Tirador): Float {
        val precios = mutableListOf<Float>()
        if (tirador.esLocal) precios.add(precioLocal)
        if (tirador.esJunior) precios.add(precioJunior)
        if (tirador.esSenior) precios.add(precioSenior)
        if (tirador.esDama) precios.add(precioDama)
        return if (precios.isEmpty()) precioGeneral else precios.min()
    }
}
