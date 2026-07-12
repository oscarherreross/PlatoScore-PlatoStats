package oscar.platoscore.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import oscar.platoscore.databinding.ActivityResultadosBinding
import oscar.platoscore.models.Clasificacion
import oscar.platoscore.models.Tirada
import oscar.platoscore.models.Tirador
import oscar.platoscore.ui.adapters.ResultadoAdapter
import oscar.platoscore.viewmodels.TiradaViewModel
import oscar.platoscore.viewmodels.TiradorViewModel

class ResultadosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultadosBinding
    private val tiradaViewModel: TiradaViewModel by viewModels()
    private val tiradorViewModel: TiradorViewModel by viewModels()

    private var tirada: Tirada? = null
    private var tiradores: List<Tirador>? = null

    /** Una clasificación de la pantalla: título, lista y filtro de categoría. */
    private class Seccion(
        val titulo: View,
        val recycler: RecyclerView,
        val adapter: ResultadoAdapter,
        val filtro: (Tirador) -> Boolean
    )

    private lateinit var secciones: List<Seccion>

    private var tiradaId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tiradaId = intent.getIntExtra("tirada_id", 0)

        setupSecciones()
        cargarResultados()
    }

    private fun setupSecciones() {
        secciones = listOf(
            crearSeccion(binding.tvTituloLocal, binding.rvResultadosLocal) { it.esLocal },
            crearSeccion(binding.tvTituloGeneral, binding.rvResultadosGeneral) { !it.esLocal },
            crearSeccion(binding.tvTituloJunior, binding.rvResultadosJunior) { it.esJunior },
            crearSeccion(binding.tvTituloSenior, binding.rvResultadosSenior) { it.esSenior },
            crearSeccion(binding.tvTituloDama, binding.rvResultadosDama) { it.esDama }
        )
    }

    private fun crearSeccion(
        titulo: View,
        recycler: RecyclerView,
        filtro: (Tirador) -> Boolean
    ): Seccion {
        val adapter = ResultadoAdapter { resultado ->
            manejarClickResultado(resultado.tirador, filtro)
        }
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)
        return Seccion(titulo, recycler, adapter, filtro)
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
            binding.tvRecaudacion.text = "No hay tiradores registrados en esta tirada."
            binding.tvHintDesempate.visibility = View.GONE
            secciones.forEach {
                it.titulo.visibility = View.GONE
                it.recycler.visibility = View.GONE
            }
            return
        }

        // Recaudación total con los precios vigentes de la tirada
        val recaudacionTotal = tiradores.sumOf { tirada.precioPara(it).toDouble() }
        binding.tvRecaudacion.text = "Recaudación total: ${"%.2f".format(recaudacionTotal)}€ · Tiradores: ${tiradores.size}"

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
            MaterialAlertDialogBuilder(this)
                .setTitle("Empate a ${tirador.platosRotos} platos")
                .setMessage("Este empate ya está resuelto. ¿Qué quieres hacer?")
                .setPositiveButton("Repetir desempate") { _, _ ->
                    pedirSiguientePuesto(ordenAlfabetico(grupo), emptyList())
                }
                .setNeutralButton("Quitar desempate") { _, _ ->
                    grupo.forEach { tiradorViewModel.updateTirador(it.copy(ordenDesempate = 0)) }
                    Toast.makeText(this, "Desempate eliminado", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            pedirSiguientePuesto(ordenAlfabetico(grupo), emptyList())
        }
    }

    /**
     * Pide al usuario, con un diálogo por puesto, el orden final de los
     * tiradores empatados (el resultado del desempate tirado en el campo).
     */
    private fun pedirSiguientePuesto(pendientes: List<Tirador>, ordenados: List<Tirador>) {
        if (pendientes.size == 1) {
            guardarDesempate(ordenados + pendientes)
            return
        }

        val nombres = pendientes.map { it.nombreApellidos }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle("Desempate a ${pendientes.first().platosRotos} platos: ¿quién queda en el puesto ${ordenados.size + 1}?")
            .setItems(nombres) { _, which ->
                pedirSiguientePuesto(
                    pendientes - pendientes[which],
                    ordenados + pendientes[which]
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun guardarDesempate(orden: List<Tirador>) {
        orden.forEachIndexed { indice, tirador ->
            tiradorViewModel.updateTirador(tirador.copy(ordenDesempate = indice + 1))
        }
        Toast.makeText(this, "Desempate guardado", Toast.LENGTH_SHORT).show()
    }

    private fun ordenAlfabetico(grupo: List<Tirador>): List<Tirador> =
        grupo.sortedBy { it.nombreApellidos.lowercase() }
}
