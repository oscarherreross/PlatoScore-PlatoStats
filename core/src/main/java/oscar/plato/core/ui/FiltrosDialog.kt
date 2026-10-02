package oscar.plato.core.ui

import android.app.DatePickerDialog
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import oscar.plato.core.R
import oscar.plato.core.databinding.DialogFiltrosBinding
import oscar.plato.core.models.FiltroTiradas
import oscar.plato.core.utils.Fechas

/** Una opción de un desplegable de filtro: el texto que se ve y el valor que se guarda. */
data class OpcionFiltro(val etiqueta: String, val valor: String)

/**
 * Diálogo con todos los filtros de un listado de tiradas. Los desplegables de
 * tipo y máquina solo aparecen si se pasan [tipos]/[maquinas] (PlatoStats);
 * cada uno incluye además la opción "Todas". El rango de fechas siempre está
 * disponible. Devuelve el filtro elegido al pulsar Aplicar (o uno vacío al
 * pulsar Limpiar).
 */
object FiltrosDialog {

    fun mostrar(
        activity: AppCompatActivity,
        filtroActual: FiltroTiradas,
        tipos: List<OpcionFiltro> = emptyList(),
        maquinas: List<OpcionFiltro> = emptyList(),
        onAplicar: (FiltroTiradas) -> Unit
    ) {
        val binding = DialogFiltrosBinding.inflate(activity.layoutInflater)

        var tipo = filtroActual.tipo
        var maquina = filtroActual.maquina
        var desde = filtroActual.desde
        var hasta = filtroActual.hasta

        if (tipos.isNotEmpty() || maquinas.isNotEmpty()) {
            val todas = activity.getString(R.string.estadisticas_filtro_todas)
            val opcionesTipo = listOf(todas to null) + tipos.map { it.etiqueta to it.valor }
            val opcionesMaquina = listOf(todas to null) + maquinas.map { it.etiqueta to it.valor }
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
