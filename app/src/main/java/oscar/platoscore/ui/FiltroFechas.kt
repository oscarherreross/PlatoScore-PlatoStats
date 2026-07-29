package oscar.platoscore.ui

import android.app.DatePickerDialog
import oscar.platoscore.databinding.ViewFiltroFechasBinding
import oscar.platoscore.utils.Fechas

/**
 * Controla un filtro por rango de fechas (Desde / Hasta) reutilizable. Cada
 * campo abre un DatePicker; el botón de la derecha limpia el rango. Notifica
 * los límites elegidos en epoch millis ([desde] = inicio del día, [hasta] =
 * fin del día); null significa sin límite por ese lado.
 */
class FiltroFechas(
    private val binding: ViewFiltroFechasBinding,
    private val onCambio: (desde: Long?, hasta: Long?) -> Unit
) {
    private var desde: Long? = null
    private var hasta: Long? = null

    init {
        binding.etDesde.setOnClickListener { elegir(esDesde = true) }
        binding.etHasta.setOnClickListener { elegir(esDesde = false) }
        binding.btnQuitarFiltro.setOnClickListener { limpiar() }
    }

    private fun elegir(esDesde: Boolean) {
        val base = (if (esDesde) desde else hasta) ?: Fechas.ahoraMillis()
        val (anio, mes, dia) = Fechas.partesDeMillis(base)
        DatePickerDialog(binding.root.context, { _, a, m, d ->
            if (esDesde) {
                desde = Fechas.inicioDelDiaMillis(a, m + 1, d)
                binding.etDesde.setText(Fechas.mostrarFecha(desde!!))
            } else {
                hasta = Fechas.finDelDiaMillis(a, m + 1, d)
                binding.etHasta.setText(Fechas.mostrarFecha(hasta!!))
            }
            onCambio(desde, hasta)
        }, anio, mes - 1, dia).show()
    }

    private fun limpiar() {
        if (desde == null && hasta == null) return
        desde = null
        hasta = null
        binding.etDesde.setText("")
        binding.etHasta.setText("")
        onCambio(null, null)
    }
}
