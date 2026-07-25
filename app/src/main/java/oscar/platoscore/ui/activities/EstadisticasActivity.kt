package oscar.platoscore.ui.activities

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import oscar.platoscore.R
import oscar.platoscore.databinding.ActivityEstadisticasBinding
import oscar.platoscore.models.EstadisticasPersonales
import oscar.platoscore.models.ResumenEstadisticas
import oscar.platoscore.models.TiradaPersonal
import oscar.platoscore.models.TiradaPersonalConSeries
import oscar.platoscore.ui.views.LineChartView
import oscar.platoscore.viewmodels.TiradaPersonalViewModel

/**
 * Evolución del tirador (rol personal). Se muestran por separado las
 * estadísticas de competición y de entrenamiento (selector superior), con la
 * gráfica de aciertos por tirada —total y al primer tiro— y la media de
 * aciertos por puesto de tiro.
 */
class EstadisticasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEstadisticasBinding
    private val viewModel: TiradaPersonalViewModel by viewModels()

    private var todas: List<TiradaPersonalConSeries> = emptyList()
    /** Tipo filtrado, o null para ver todas las tiradas juntas. */
    private var tipoSeleccionado: String? = null

    private var colorTotal = 0
    private var colorPrimerTiro = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEstadisticasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setTitle(R.string.estadisticas_titulo)

        colorTotal = resolverColorPrimario()
        colorPrimerTiro = ContextCompat.getColor(this, R.color.chart_primer_tiro)
        prepararLeyenda()

        configurarFiltro()

        viewModel.todas.observe(this) { lista ->
            todas = lista
            render()
        }
    }

    private fun configurarFiltro() {
        // Cada posición del desplegable se asocia a un tipo filtrado (null = todas).
        val opciones = listOf(
            getString(R.string.estadisticas_filtro_todas) to null,
            getString(R.string.tipo_entrenamiento) to TiradaPersonal.TIPO_ENTRENAMIENTO,
            getString(R.string.tipo_competicion) to TiradaPersonal.TIPO_COMPETICION
        )
        binding.dropdownTipo.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, opciones.map { it.first })
        )
        // Selección inicial: Todas (sin disparar filtrado de texto).
        binding.dropdownTipo.setText(opciones.first().first, false)
        binding.dropdownTipo.setOnItemClickListener { _, _, posicion, _ ->
            tipoSeleccionado = opciones[posicion].second
            render()
        }
    }

    private fun render() {
        val sel = tipoSeleccionado
        val filtradas = if (sel == null) todas else todas.filter { it.tirada.tipo == sel }
        mostrar(EstadisticasPersonales.calcular(filtradas))
    }

    private fun mostrar(resumen: ResumenEstadisticas) {
        if (!resumen.hayDatos) {
            binding.tvVacio.visibility = View.VISIBLE
            binding.contenido.visibility = View.GONE
            return
        }
        binding.tvVacio.visibility = View.GONE
        binding.contenido.visibility = View.VISIBLE

        binding.tvNumTiradas.text =
            getString(R.string.estadisticas_num_tiradas, resumen.numTiradas)
        binding.tvMediaPlatos.text =
            getString(R.string.estadisticas_media_platos, "%.1f".format(resumen.mediaPlatos))
        binding.tvMediaPorcentaje.text =
            getString(R.string.estadisticas_media_porcentaje, "%.1f".format(resumen.mediaPorcentaje))
        binding.tvMejor.text =
            getString(R.string.estadisticas_mejor, "%.1f".format(resumen.mejorPorcentaje))

        val mediaPrimer = resumen.mediaPorcentajePrimerTiro
        if (mediaPrimer != null) {
            binding.tvMediaPrimerTiro.visibility = View.VISIBLE
            binding.tvMediaPrimerTiro.text =
                getString(R.string.estadisticas_media_primer_tiro, "%.1f".format(mediaPrimer))
        } else {
            binding.tvMediaPrimerTiro.visibility = View.GONE
        }

        mostrarGrafica(resumen)
        mostrarPorPuesto(resumen)
    }

    private fun mostrarGrafica(resumen: ResumenEstadisticas) {
        val series = mutableListOf(
            LineChartView.Serie(resumen.puntos.map { it.porcentaje }, colorTotal)
        )
        val hayPrimerTiro = resumen.puntos.any { it.porcentajePrimerTiro != null }
        if (hayPrimerTiro) {
            series.add(
                LineChartView.Serie(resumen.puntos.map { it.porcentajePrimerTiro }, colorPrimerTiro)
            )
        }
        binding.tvLeyendaPrimerTiro.visibility = if (hayPrimerTiro) View.VISIBLE else View.GONE
        binding.chart.setSeries(series)
    }

    private fun mostrarPorPuesto(resumen: ResumenEstadisticas) {
        binding.containerPuestos.removeAllViews()
        resumen.mediaPorPuesto.forEach { puesto ->
            val tv = TextView(this)
            tv.textSize = 15f
            tv.setPadding(0, dp(2), 0, dp(2))
            tv.text = getString(
                R.string.estadisticas_puesto_linea,
                puesto.puesto,
                "%.1f".format(puesto.porcentaje),
                puesto.numSeries
            )
            binding.containerPuestos.addView(tv)
        }
    }

    private fun prepararLeyenda() {
        binding.tvLeyendaTotal.text = "● " + getString(R.string.estadisticas_leyenda_total)
        binding.tvLeyendaTotal.setTextColor(colorTotal)
        binding.tvLeyendaPrimerTiro.text = "● " + getString(R.string.estadisticas_leyenda_primer_tiro)
        binding.tvLeyendaPrimerTiro.setTextColor(colorPrimerTiro)
    }

    private fun resolverColorPrimario(): Int {
        val tv = TypedValue()
        val ok = theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, tv, true)
        return if (ok) tv.data else ContextCompat.getColor(this, R.color.purple_500)
    }

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).toInt()
}
