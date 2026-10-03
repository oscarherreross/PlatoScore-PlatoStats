package oscar.platostats.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import oscar.plato.core.models.FiltroTiradas
import oscar.plato.core.ui.DialogosRestaurables
import oscar.plato.core.utils.Fechas
import oscar.plato.core.utils.InsetsUtil
import oscar.plato.core.utils.enableEdgeToEdgeConToolbar
import oscar.platostats.R
import oscar.platostats.databinding.ActivityEstadisticasBinding
import oscar.platostats.models.Estadisticas
import oscar.platostats.models.ResumenEstadisticas
import oscar.platostats.models.Tirada
import oscar.platostats.models.TiradaConSeries
import oscar.platostats.models.acepta
import oscar.platostats.ui.Filtros
import oscar.platostats.ui.views.LineChartView
import oscar.platostats.viewmodels.TiradaViewModel
import oscar.plato.core.R as CoreR

/**
 * Evolución del tirador. Se muestran por separado las
 * estadísticas de competición y de entrenamiento (selector superior), con la
 * gráfica de aciertos por tirada —total y al primer tiro— y la media de
 * aciertos por puesto de tiro.
 */
class EstadisticasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEstadisticasBinding
    private val viewModel: TiradaViewModel by viewModels()

    private val dialogos = DialogosRestaurables(this)

    private var todas: List<TiradaConSeries> = emptyList()
    private var ultimoResumen: ResumenEstadisticas? = null

    /** El filtro vive en el ViewModel para sobrevivir a la recreación de la pantalla. */
    private val filtro: FiltroTiradas
        get() = viewModel.filtro.value ?: FiltroTiradas()

    private var colorTotal = 0
    private var colorPrimerTiro = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeConToolbar()
        binding = ActivityEstadisticasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        InsetsUtil.padTop(binding.toolbar)
        InsetsUtil.padBottom(binding.contenidoScrollEstadisticas)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setTitle(R.string.estadisticas_titulo)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        colorTotal = resolverColorPrimario()
        colorPrimerTiro = ContextCompat.getColor(this, CoreR.color.chart_primer_tiro)
        prepararLeyenda()
        binding.btnCompartir.setOnClickListener { compartir() }

        // Registrado aquí, el diálogo sigue abierto si la pantalla se recrea (giro).
        Filtros.registrar(this, dialogos) { nuevo -> viewModel.filtro.value = nuevo }

        viewModel.todas.observe(this) { lista ->
            todas = lista
            render()
        }
        viewModel.filtro.observe(this) { render() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(CoreR.menu.menu_filtro, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            CoreR.id.action_filtros -> {
                Filtros.mostrar(dialogos, filtro)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun render() {
        mostrar(Estadisticas.calcular(todas.filter { filtro.acepta(it) }))
    }

    private fun mostrar(resumen: ResumenEstadisticas) {
        ultimoResumen = resumen
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
        mostrarPorMaquina(resumen)
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

    private fun mostrarPorMaquina(resumen: ResumenEstadisticas) {
        binding.containerMaquinas.removeAllViews()
        resumen.mediaPorMaquina.forEach { maquina ->
            val tv = TextView(this)
            tv.textSize = 15f
            tv.setPadding(0, dp(2), 0, dp(2))
            tv.text = getString(
                R.string.estadisticas_maquina_linea,
                etiquetaMaquina(maquina.maquina),
                "%.1f".format(maquina.porcentaje),
                maquina.numTiradas
            )
            binding.containerMaquinas.addView(tv)
        }
    }

    private fun compartir() {
        val texto = generarTexto() ?: run {
            Toast.makeText(this, CoreR.string.toast_nada_que_compartir, Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texto)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.btn_compartir_estadisticas)))
    }

    /** Genera el resumen de estadísticas en texto plano, respetando el filtro activo. */
    private fun generarTexto(): String? {
        val resumen = ultimoResumen
        if (resumen == null || !resumen.hayDatos) return null

        val sb = StringBuilder()
        sb.appendLine(getString(R.string.compartir_estadisticas_cabecera))
        if (filtro.activo) {
            val partes = mutableListOf<String>()
            filtro.tipo?.let { partes.add(etiquetaTipo(it)) }
            filtro.maquina?.let { partes.add(etiquetaMaquina(it)) }
            if (filtro.desde != null || filtro.hasta != null) {
                val desde = filtro.desde?.let { Fechas.mostrarFecha(it) } ?: "…"
                val hasta = filtro.hasta?.let { Fechas.mostrarFecha(it) } ?: "…"
                partes.add("$desde – $hasta")
            }
            sb.appendLine(getString(R.string.compartir_estadisticas_filtro, partes.joinToString(" · ")))
        }

        sb.appendLine()
        sb.appendLine(getString(R.string.estadisticas_num_tiradas, resumen.numTiradas))
        sb.appendLine(getString(R.string.estadisticas_media_platos, "%.1f".format(resumen.mediaPlatos)))
        sb.appendLine(getString(R.string.estadisticas_media_porcentaje, "%.1f".format(resumen.mediaPorcentaje)))
        sb.appendLine(getString(R.string.estadisticas_mejor, "%.1f".format(resumen.mejorPorcentaje)))
        resumen.mediaPorcentajePrimerTiro?.let {
            sb.appendLine(getString(R.string.estadisticas_media_primer_tiro, "%.1f".format(it)))
        }

        if (resumen.mediaPorPuesto.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine(getString(R.string.estadisticas_por_puesto_titulo) + ":")
            resumen.mediaPorPuesto.forEach {
                sb.appendLine(
                    getString(
                        R.string.estadisticas_puesto_linea,
                        it.puesto, "%.1f".format(it.porcentaje), it.numSeries
                    )
                )
            }
        }
        if (resumen.mediaPorMaquina.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine(getString(R.string.estadisticas_por_maquina_titulo) + ":")
            resumen.mediaPorMaquina.forEach {
                sb.appendLine(
                    getString(
                        R.string.estadisticas_maquina_linea,
                        etiquetaMaquina(it.maquina), "%.1f".format(it.porcentaje), it.numTiradas
                    )
                )
            }
        }

        sb.appendLine()
        sb.appendLine(getString(R.string.estadisticas_grafica_titulo) + ":")
        resumen.puntos.forEach { p ->
            sb.appendLine(
                getString(
                    R.string.compartir_estadisticas_evolucion_linea,
                    Fechas.mostrarFecha(p.fechaHora), "%.1f".format(p.porcentaje)
                )
            )
        }
        return sb.toString().trimEnd()
    }

    private fun etiquetaTipo(tipo: String): String = getString(
        if (tipo == Tirada.TIPO_COMPETICION) R.string.tipo_competicion
        else R.string.tipo_entrenamiento
    )

    private fun etiquetaMaquina(maquina: String): String = getString(
        when (maquina) {
            Tirada.MAQUINA_TRAP -> R.string.maquina_trap
            Tirada.MAQUINA_OLIMPICO -> R.string.maquina_olimpico
            else -> R.string.maquina_robot
        }
    )

    private fun prepararLeyenda() {
        binding.tvLeyendaTotal.text = "● " + getString(R.string.estadisticas_leyenda_total)
        binding.tvLeyendaTotal.setTextColor(colorTotal)
        binding.tvLeyendaPrimerTiro.text = "● " + getString(R.string.estadisticas_leyenda_primer_tiro)
        binding.tvLeyendaPrimerTiro.setTextColor(colorPrimerTiro)
    }

    private fun resolverColorPrimario(): Int {
        val tv = TypedValue()
        val ok = theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, tv, true)
        return if (ok) tv.data else ContextCompat.getColor(this, CoreR.color.naranja)
    }

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).toInt()
}
