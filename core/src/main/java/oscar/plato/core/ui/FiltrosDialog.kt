package oscar.plato.core.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
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
 *
 * La pantalla lo registra en su onCreate ([registrar]), para que siga abierto si
 * se recrea, y lo abre con [mostrar].
 */
object FiltrosDialog {

    private const val DIALOGO = "filtros"
    private const val ARG_FILTRO = "filtro"

    fun registrar(
        activity: AppCompatActivity,
        dialogos: DialogosRestaurables,
        tipos: List<OpcionFiltro> = emptyList(),
        maquinas: List<OpcionFiltro> = emptyList(),
        onAplicar: (FiltroTiradas) -> Unit
    ) {
        dialogos.registrar(DIALOGO) { args ->
            crear(activity, dialogos, args, tipos, maquinas, onAplicar)
        }
    }

    /** Abre el diálogo partiendo de [filtroActual]. */
    fun mostrar(dialogos: DialogosRestaurables, filtroActual: FiltroTiradas) {
        dialogos.mostrar(DIALOGO, bundleOf(ARG_FILTRO to filtroActual))
    }

    private fun crear(
        activity: AppCompatActivity,
        dialogos: DialogosRestaurables,
        args: Bundle,
        tipos: List<OpcionFiltro>,
        maquinas: List<OpcionFiltro>,
        onAplicar: (FiltroTiradas) -> Unit
    ): Dialog {
        val binding = DialogFiltrosBinding.inflate(activity.layoutInflater)

        // La selección en curso se anota en los argumentos, para no perderla si la
        // pantalla se recrea con el diálogo abierto.
        var filtro = BundleCompat.getParcelable(args, ARG_FILTRO, FiltroTiradas::class.java)
            ?: FiltroTiradas()
        fun cambiar(nuevo: FiltroTiradas) {
            filtro = nuevo
            args.putParcelable(ARG_FILTRO, nuevo)
        }

        if (tipos.isNotEmpty() || maquinas.isNotEmpty()) {
            val todas = activity.getString(R.string.estadisticas_filtro_todas)
            val opcionesTipo = listOf(todas to null) + tipos.map { it.etiqueta to it.valor }
            val opcionesMaquina = listOf(todas to null) + maquinas.map { it.etiqueta to it.valor }
            binding.dropdownTipo.setAdapter(
                ArrayAdapter(activity, android.R.layout.simple_list_item_1, opcionesTipo.map { it.first })
            )
            binding.dropdownTipo.setText(etiqueta(opcionesTipo, filtro.tipo), false)
            binding.dropdownTipo.setOnItemClickListener { _, _, pos, _ ->
                cambiar(filtro.copy(tipo = opcionesTipo[pos].second))
            }

            binding.dropdownMaquina.setAdapter(
                ArrayAdapter(activity, android.R.layout.simple_list_item_1, opcionesMaquina.map { it.first })
            )
            binding.dropdownMaquina.setText(etiqueta(opcionesMaquina, filtro.maquina), false)
            binding.dropdownMaquina.setOnItemClickListener { _, _, pos, _ ->
                cambiar(filtro.copy(maquina = opcionesMaquina[pos].second))
            }
        } else {
            binding.contenedorTipoMaquina.visibility = View.GONE
        }

        fun refrescarFechas() {
            binding.etDesde.setText(filtro.desde?.let { Fechas.mostrarFecha(it) } ?: "")
            binding.etHasta.setText(filtro.hasta?.let { Fechas.mostrarFecha(it) } ?: "")
        }
        refrescarFechas()
        binding.etDesde.setOnClickListener {
            elegirFecha(activity, dialogos, filtro.desde, esFin = false) { millis ->
                cambiar(filtro.copy(desde = millis))
                refrescarFechas()
            }
        }
        binding.etHasta.setOnClickListener {
            elegirFecha(activity, dialogos, filtro.hasta, esFin = true) { millis ->
                cambiar(filtro.copy(hasta = millis))
                refrescarFechas()
            }
        }

        return MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.menu_filtros)
            .setView(binding.root)
            .setNeutralButton(R.string.accion_limpiar) { _, _ -> onAplicar(FiltroTiradas()) }
            .setNegativeButton(R.string.accion_cancelar, null)
            .setPositiveButton(R.string.accion_aplicar) { _, _ -> onAplicar(filtro) }
            .create()
    }

    private fun <T> etiqueta(opciones: List<Pair<String, T>>, valor: T): String =
        opciones.firstOrNull { it.second == valor }?.first ?: opciones.first().first

    private fun elegirFecha(
        activity: AppCompatActivity,
        dialogos: DialogosRestaurables,
        base: Long?,
        esFin: Boolean,
        onElegida: (Long) -> Unit
    ) {
        val (anio, mes, dia) = Fechas.partesDeMillis(base ?: Fechas.ahoraMillis())
        dialogos.mostrarDePaso(DatePickerDialog(activity, { _, a, m, d ->
            onElegida(
                if (esFin) Fechas.finDelDiaMillis(a, m + 1, d)
                else Fechas.inicioDelDiaMillis(a, m + 1, d)
            )
        }, anio, mes - 1, dia))
    }
}
