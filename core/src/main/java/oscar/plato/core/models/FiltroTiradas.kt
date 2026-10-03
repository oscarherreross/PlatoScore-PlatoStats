package oscar.plato.core.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Filtros aplicables a un listado de tiradas. Todos son opcionales (null = sin
 * filtrar por ese campo). El rango de fechas se expresa en epoch millis:
 * [desde] = inicio del día, [hasta] = fin del día.
 *
 * Es común a las dos apps; cada una define qué tiradas lo cumplen con su propia
 * función `acepta` (PlatoScore solo filtra por fechas; PlatoStats también por
 * tipo y máquina).
 *
 * Es Parcelable para poder guardarlo con el estado de la pantalla (giro, cierre
 * del proceso).
 */
@Parcelize
data class FiltroTiradas(
    val tipo: String? = null,
    val maquina: String? = null,
    val desde: Long? = null,
    val hasta: Long? = null
) : Parcelable {
    val activo: Boolean
        get() = tipo != null || maquina != null || desde != null || hasta != null
}
