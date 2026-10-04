package oscar.platoscore.ui.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import oscar.plato.core.ui.DialogosRestaurables
import oscar.plato.core.ui.exigirSesion
import oscar.plato.core.utils.Fechas
import oscar.plato.core.utils.InsetsUtil
import oscar.plato.core.utils.enableEdgeToEdgeConToolbar
import oscar.platoscore.R
import oscar.platoscore.databinding.ActivityResultadosBinding
import oscar.platoscore.models.Clasificacion
import oscar.platoscore.models.Tirada
import oscar.platoscore.models.Tirador
import oscar.platoscore.ui.adapters.ResultadoAdapter
import oscar.platoscore.utils.Extras
import oscar.platoscore.viewmodels.TiradaViewModel
import oscar.platoscore.viewmodels.TiradorViewModel
import oscar.plato.core.R as CoreR

class ResultadosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultadosBinding
    private val tiradaViewModel: TiradaViewModel by viewModels()
    private val tiradorViewModel: TiradorViewModel by viewModels()

    private val dialogos = DialogosRestaurables(this)

    private var tirada: Tirada? = null
    private var tiradores: List<Tirador>? = null

    /** Una clasificación de la pantalla: título, lista y filtro de categoría. */
    private class Seccion(
        val nombre: String,
        val titulo: View,
        val recycler: RecyclerView,
        val adapter: ResultadoAdapter,
        val filtro: (Tirador) -> Boolean
    )

    private lateinit var secciones: List<Seccion>

    private var tiradaId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!exigirSesion()) return
        enableEdgeToEdgeConToolbar()
        binding = ActivityResultadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        InsetsUtil.padTop(binding.toolbar)
        InsetsUtil.padBottom(binding.contenidoResultados)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setTitle(R.string.titulo_resultados)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tiradaId = intent.getIntExtra(Extras.TIRADA_ID, 0)

        // Registrados aquí, los diálogos de desempate siguen abiertos si la pantalla
        // se recrea (giro). Llevan a los tiradores afectados en sus argumentos.
        dialogos.registrar(DIALOGO_EMPATE_RESUELTO) { args ->
            crearDialogoEmpateResuelto(tiradoresDe(args, ARG_GRUPO))
        }
        dialogos.registrar(DIALOGO_PUESTO) { args ->
            crearDialogoPuesto(tiradoresDe(args, ARG_PENDIENTES), tiradoresDe(args, ARG_ORDENADOS))
        }

        setupSecciones()
        setupCompartir()
        cargarResultados()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupSecciones() {
        secciones = listOf(
            crearSeccion(getString(R.string.clasificacion_local), binding.tvTituloLocal, binding.rvResultadosLocal) { it.esLocal },
            crearSeccion(getString(R.string.clasificacion_general), binding.tvTituloGeneral, binding.rvResultadosGeneral) { !it.esLocal },
            crearSeccion(getString(R.string.clasificacion_junior), binding.tvTituloJunior, binding.rvResultadosJunior) { it.esJunior },
            crearSeccion(getString(R.string.clasificacion_senior), binding.tvTituloSenior, binding.rvResultadosSenior) { it.esSenior },
            crearSeccion(getString(R.string.clasificacion_dama), binding.tvTituloDama, binding.rvResultadosDama) { it.esDama }
        )
    }

    private fun crearSeccion(
        nombre: String,
        titulo: View,
        recycler: RecyclerView,
        filtro: (Tirador) -> Boolean
    ): Seccion {
        val adapter = ResultadoAdapter { resultado ->
            manejarClickResultado(resultado.tirador, filtro)
        }
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)
        return Seccion(nombre, titulo, recycler, adapter, filtro)
    }

    private fun setupCompartir() {
        binding.btnCompartir.setOnClickListener {
            val texto = generarTextoResultados()
            if (texto == null) {
                Toast.makeText(this, CoreR.string.toast_nada_que_compartir, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, texto)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.btn_compartir_resultados)))
        }
    }

    /**
     * Clasificaciones en texto plano para compartir. No incluye la
     * recaudación ni los precios: son datos internos de la organización.
     */
    private fun generarTextoResultados(): String? {
        val tirada = this.tirada ?: return null
        val tiradores = this.tiradores ?: return null
        if (tiradores.isEmpty()) return null

        val sb = StringBuilder()
        sb.appendLine(getString(R.string.compartir_cabecera, tirada.nombre, Fechas.mostrar(tirada.fecha)))
        sb.appendLine(getString(R.string.compartir_num_tiradores, tiradores.size))

        secciones.forEach { seccion ->
            val resultados = Clasificacion.generar(tiradores.filter(seccion.filtro), tirada)
            if (resultados.isNotEmpty()) {
                sb.appendLine()
                sb.appendLine("${seccion.nombre}:")
                resultados.forEach { r ->
                    val marcaEmpate = if (r.empatado) "=" else ""
                    sb.appendLine(
                        getString(
                            R.string.compartir_linea_resultado,
                            marcaEmpate, r.posicion, r.tirador.nombreApellidos, r.tirador.platosRotos
                        )
                    )
                }
            }
        }
        return sb.toString().trimEnd()
    }

    private fun cargarResultados() {
        tiradaViewModel.getTirada(tiradaId).observe(this) { t ->
            tirada = t
            renderizarResultados()
        }

        tiradorViewModel.getTiradoresByTirada(tiradaId).observe(this) { lista ->
            tiradores = lista
            renderizarResultados()
        }
    }

    private fun renderizarResultados() {
        val tirada = this.tirada ?: return
        val tiradores = this.tiradores ?: return

        if (tiradores.isEmpty()) {
            binding.tvRecaudacion.text = getString(R.string.msg_sin_tiradores_tirada)
            binding.tvHintDesempate.visibility = View.GONE
            secciones.forEach {
                it.titulo.visibility = View.GONE
                it.recycler.visibility = View.GONE
            }
            return
        }

        // Recaudación total con los precios vigentes de la tirada
        val recaudacionTotal = tiradores.sumOf { tirada.precioPara(it).toDouble() }
        binding.tvRecaudacion.text =
            getString(R.string.msg_recaudacion, "%.2f".format(recaudacionTotal), tiradores.size)

        var hayEmpates = false
        secciones.forEach { seccion ->
            val resultados = Clasificacion.generar(tiradores.filter(seccion.filtro), tirada)
            if (resultados.any { it.empatado }) hayEmpates = true
            seccion.adapter.submitList(resultados)
            val visibilidad = if (resultados.isEmpty()) View.GONE else View.VISIBLE
            seccion.titulo.visibility = visibilidad
            seccion.recycler.visibility = visibilidad
        }
        binding.tvHintDesempate.visibility = if (hayEmpates) View.VISIBLE else View.GONE
    }

    // ─── DESEMPATES ───

    private fun manejarClickResultado(tirador: Tirador, filtro: (Tirador) -> Boolean) {
        val grupo = tiradores.orEmpty()
            .filter(filtro)
            .filter { it.platosRotos == tirador.platosRotos }
        if (grupo.size < 2) return

        val yaResuelto = grupo.none { it.ordenDesempate == 0 }
        if (yaResuelto) {
            dialogos.mostrar(DIALOGO_EMPATE_RESUELTO, Bundle().apply {
                putParcelableArrayList(ARG_GRUPO, ArrayList(grupo))
            })
        } else {
            pedirSiguientePuesto(ordenAlfabetico(grupo), emptyList())
        }
    }

    private fun crearDialogoEmpateResuelto(grupo: List<Tirador>): Dialog =
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.titulo_empate, grupo.first().platosRotos))
            .setMessage(R.string.msg_empate_resuelto)
            .setPositiveButton(R.string.accion_repetir_desempate) { _, _ ->
                pedirSiguientePuesto(ordenAlfabetico(grupo), emptyList())
            }
            .setNeutralButton(R.string.accion_quitar_desempate) { _, _ ->
                grupo.forEach { tiradorViewModel.updateTirador(it.copy(ordenDesempate = 0)) }
                Toast.makeText(this, R.string.toast_desempate_eliminado, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(CoreR.string.accion_cancelar, null)
            .create()

    /**
     * Pide al usuario, con un diálogo por puesto, el orden final de los
     * tiradores empatados (el resultado del desempate tirado en el campo).
     */
    private fun pedirSiguientePuesto(pendientes: List<Tirador>, ordenados: List<Tirador>) {
        if (pendientes.size == 1) {
            guardarDesempate(ordenados + pendientes)
            return
        }

        dialogos.mostrar(DIALOGO_PUESTO, Bundle().apply {
            putParcelableArrayList(ARG_PENDIENTES, ArrayList(pendientes))
            putParcelableArrayList(ARG_ORDENADOS, ArrayList(ordenados))
        })
    }

    private fun crearDialogoPuesto(pendientes: List<Tirador>, ordenados: List<Tirador>): Dialog {
        val nombres = pendientes.map { it.nombreApellidos }.toTypedArray()
        return MaterialAlertDialogBuilder(this)
            .setTitle(
                getString(
                    R.string.titulo_desempate_puesto,
                    pendientes.first().platosRotos,
                    ordenados.size + 1
                )
            )
            .setItems(nombres) { _, which ->
                pedirSiguientePuesto(
                    pendientes - pendientes[which],
                    ordenados + pendientes[which]
                )
            }
            .setNegativeButton(CoreR.string.accion_cancelar, null)
            .create()
    }

    private fun tiradoresDe(args: Bundle, clave: String): List<Tirador> =
        BundleCompat.getParcelableArrayList(args, clave, Tirador::class.java).orEmpty()

    private fun guardarDesempate(orden: List<Tirador>) {
        orden.forEachIndexed { indice, tirador ->
            tiradorViewModel.updateTirador(tirador.copy(ordenDesempate = indice + 1))
        }
        Toast.makeText(this, R.string.toast_desempate_guardado, Toast.LENGTH_SHORT).show()
    }

    private fun ordenAlfabetico(grupo: List<Tirador>): List<Tirador> =
        grupo.sortedBy { it.nombreApellidos.lowercase() }

    private companion object {
        const val DIALOGO_EMPATE_RESUELTO = "empate_resuelto"
        const val DIALOGO_PUESTO = "desempate_puesto"
        const val ARG_GRUPO = "grupo"
        const val ARG_PENDIENTES = "pendientes"
        const val ARG_ORDENADOS = "ordenados"
    }
}
