package oscar.platoscore.ui

import android.app.DatePickerDialog
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import oscar.platoscore.R
import oscar.platoscore.databinding.DialogFiltrosBinding
import oscar.platoscore.models.FiltroTiradas
import oscar.platoscore.models.TiradaPersonal
import oscar.platoscore.utils.Fechas

/**
 * Diálogo con todos los filtros de un listado de tiradas. Los filtros de tipo y
 * máquina solo se muestran si [conTipoYMaquina] es true (tiradas personales); el
 * rango de fechas siempre está disponible. Devuelve el filtro elegido al pulsar
 * Aplicar (o uno vacío al pulsar Limpiar).
 */
object FiltrosDialog {

    fun mostrar(
        activity: AppCompatActivity,
        filtroActual: FiltroTiradas,
        conTipoYMaquina: Boolean,
        onAplicar: (FiltroTiradas) -> Unit
    ) {
        val binding = DialogFiltrosBinding.inflate(activity.layoutInflater)

        var tipo = filtroActual.tipo
        var maquina = filtroActual.maquina
        var desde = filtroActual.desde
        var hasta = filtroActual.hasta

        if (conTipoYMaquina) {
            val opcionesTipo = listOf(
                activity.getString(R.string.estadisticas_filtro_todas) to null,
                activity.getString(R.string.tipo_entrenamiento) to TiradaPersonal.TIPO_ENTRENAMIENTO,
                activity.getString(R.string.tipo_competicion) to TiradaPersonal.TIPO_COMPETICION
            )
            val opcionesMaquina = listOf(
                activity.getString(R.string.estadisticas_filtro_todas) to null,
                activity.getString(R.string.maquina_robot) to TiradaPersonal.MAQUINA_ROBOT,
                activity.getString(R.string.maquina_trap) to TiradaPersonal.MAQUINA_TRAP,
                activity.getString(R.string.maquina_olimpico) to TiradaPersonal.MAQUINA_OLIMPICO
            )
            binding.dropdownTipo.setAdapter(
                ArrayAdapter(activity, android.R.layout.simple_list_item_1, opcionesTipo.map { it.first })
            )
            binding.dropdownTipo.setText(etiqueta(opcionesTipo, tipo), false)
            binding.dropdownTipo.setOnItemClickListener { _, _, pos, _ -> tipo = opcionesTipo[pos].second }

            binding.dropdownMaquina.setAdapter(
                ArrayAdapter(activity, android.R.layout.simple_list_item_1, opcionesMaquina.map { it.first })
            )
            binding.dropdownMaquina.setText(etiqueta(opcionesMaquina, maquina), false)
            binding.dropdownMaquina.setOnItemClickListener { _, _, pos, _ -> maquina = opcionesMaquina[pos].second }
        } else {
            binding.contenedorTipoMaquina.visibility = View.GONE
        }

        fun refrescarFechas() {
            binding.etDesde.setText(desde?.let { Fechas.mostrarFecha(it) } ?: "")
            binding.etHasta.setText(hasta?.let { Fechas.mostrarFecha(it) } ?: "")
        }
        refrescarFechas()
        binding.etDesde.setOnClickListener {
            elegirFecha(activity, desde, esFin = false) { millis -> desde = millis; refrescarFechas() }
        }
        binding.etHasta.setOnClickListener {
            elegirFecha(activity, hasta, esFin = true) { millis -> hasta = millis; refrescarFechas() }
        }

        MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.menu_filtros)
            .setView(binding.root)
            .setNeutralButton(R.string.accion_limpiar) { _, _ -> onAplicar(FiltroTiradas()) }
            .setNegativeButton(R.string.accion_cancelar, null)
            .setPositiveButton(R.string.accion_aplicar) { _, _ ->
                onAplicar(FiltroTiradas(tipo = tipo, maquina = maquina, desde = desde, hasta = hasta))
            }
            .show()
    }

    private fun <T> etiqueta(opciones: List<Pair<String, T>>, valor: T): String =
        opciones.firstOrNull { it.second == valor }?.first ?: opciones.first().first

    private fun elegirFecha(
        activity: AppCompatActivity,
        base: Long?,
        esFin: Boolean,
        onElegida: (Long) -> Unit
    ) {
        val (anio, mes, dia) = Fechas.partesDeMillis(base ?: Fechas.ahoraMillis())
        DatePickerDialog(activity, { _, a, m, d ->
            onElegida(
                if (esFin) Fechas.finDelDiaMillis(a, m + 1, d)
                else Fechas.inicioDelDiaMillis(a, m + 1, d)
            )
        }, anio, mes - 1, dia).show()
    }
}
