package oscar.platostats.ui

import androidx.appcompat.app.AppCompatActivity
import oscar.plato.core.models.FiltroTiradas
import oscar.plato.core.ui.FiltrosDialog
import oscar.plato.core.ui.OpcionFiltro
import oscar.platostats.R
import oscar.platostats.models.Tirada

/** Diálogo de filtros de PlatoStats: tipo de tirada, máquina y rango de fechas. */
object Filtros {

    fun mostrar(
        activity: AppCompatActivity,
        filtroActual: FiltroTiradas,
        onAplicar: (FiltroTiradas) -> Unit
    ) {
        FiltrosDialog.mostrar(
            activity,
            filtroActual,
            tipos = listOf(
                OpcionFiltro(activity.getString(R.string.tipo_entrenamiento), Tirada.TIPO_ENTRENAMIENTO),
                OpcionFiltro(activity.getString(R.string.tipo_competicion), Tirada.TIPO_COMPETICION)
            ),
            maquinas = listOf(
                OpcionFiltro(activity.getString(R.string.maquina_robot), Tirada.MAQUINA_ROBOT),
                OpcionFiltro(activity.getString(R.string.maquina_trap), Tirada.MAQUINA_TRAP),
                OpcionFiltro(activity.getString(R.string.maquina_olimpico), Tirada.MAQUINA_OLIMPICO)
            ),
            onAplicar = onAplicar
        )
    }
}
