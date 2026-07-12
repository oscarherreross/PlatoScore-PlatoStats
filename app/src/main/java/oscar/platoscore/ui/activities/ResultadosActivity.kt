package oscar.platoscore.ui.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import oscar.platoscore.databinding.ActivityResultadosBinding
import oscar.platoscore.models.Resultado
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

    private lateinit var resultadoLocalAdapter: ResultadoAdapter
    private lateinit var resultadoGeneralAdapter: ResultadoAdapter
    private lateinit var resultadoJuniorAdapter: ResultadoAdapter
    private lateinit var resultadoSeniorAdapter: ResultadoAdapter
    private lateinit var resultadoDamaAdapter: ResultadoAdapter

    private var tiradaId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tiradaId = intent.getIntExtra("tirada_id", 0)

        setupRecyclerViews()
        cargarResultados()
    }

    private fun setupRecyclerViews() {
        resultadoLocalAdapter = ResultadoAdapter()
        resultadoGeneralAdapter = ResultadoAdapter()
        resultadoJuniorAdapter = ResultadoAdapter()
        resultadoSeniorAdapter = ResultadoAdapter()
        resultadoDamaAdapter = ResultadoAdapter()

        binding.rvResultadosLocal.apply {
            adapter = resultadoLocalAdapter
            layoutManager = LinearLayoutManager(this@ResultadosActivity)
        }

        binding.rvResultadosGeneral.apply {
            adapter = resultadoGeneralAdapter
            layoutManager = LinearLayoutManager(this@ResultadosActivity)
        }

        binding.rvResultadosJunior.apply {
            adapter = resultadoJuniorAdapter
            layoutManager = LinearLayoutManager(this@ResultadosActivity)
        }

        binding.rvResultadosSenior.apply {
            adapter = resultadoSeniorAdapter
            layoutManager = LinearLayoutManager(this@ResultadosActivity)
        }

        binding.rvResultadosDama.apply {
            adapter = resultadoDamaAdapter
            layoutManager = LinearLayoutManager(this@ResultadosActivity)
        }
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
            return
        }

        // Recaudación total con los precios vigentes de la tirada
        val recaudacionTotal = tiradores.sumOf { tirada.precioPara(it).toDouble() }
        binding.tvRecaudacion.text = "Recaudación total: ${"%.2f".format(recaudacionTotal)}€ · Tiradores: ${tiradores.size}"

        // Clasificación Local (tiradores locales, ordenados por platos rotos desc)
        val locales = tiradores
            .filter { it.esLocal }
            .sortedByDescending { it.platosRotos }
            .map { Resultado(tirador = it, precio = tirada.precioPara(it)) }
        resultadoLocalAdapter.submitList(locales)
        mostrarOcultar(binding.tvTituloLocal, binding.rvResultadosLocal, locales)

        // Clasificación General (todos los NO locales, ordenados por platos rotos desc)
        val generales = tiradores
            .filter { !it.esLocal }
            .sortedByDescending { it.platosRotos }
            .map { Resultado(tirador = it, precio = tirada.precioPara(it)) }
        resultadoGeneralAdapter.submitList(generales)
        mostrarOcultar(binding.tvTituloGeneral, binding.rvResultadosGeneral, generales)

        // Clasificación Junior
        val juniors = tiradores
            .filter { it.esJunior }
            .sortedByDescending { it.platosRotos }
            .map { Resultado(tirador = it, precio = tirada.precioPara(it)) }
        resultadoJuniorAdapter.submitList(juniors)
        mostrarOcultar(binding.tvTituloJunior, binding.rvResultadosJunior, juniors)

        // Clasificación Senior
        val seniors = tiradores
            .filter { it.esSenior }
            .sortedByDescending { it.platosRotos }
            .map { Resultado(tirador = it, precio = tirada.precioPara(it)) }
        resultadoSeniorAdapter.submitList(seniors)
        mostrarOcultar(binding.tvTituloSenior, binding.rvResultadosSenior, seniors)

        // Clasificación Dama
        val damas = tiradores
            .filter { it.esDama }
            .sortedByDescending { it.platosRotos }
            .map { Resultado(tirador = it, precio = tirada.precioPara(it)) }
        resultadoDamaAdapter.submitList(damas)
        mostrarOcultar(binding.tvTituloDama, binding.rvResultadosDama, damas)
    }

    private fun mostrarOcultar(titulo: View, recycler: View, lista: List<Resultado>) {
        val visibilidad = if (lista.isEmpty()) View.GONE else View.VISIBLE
        titulo.visibility = visibilidad
        recycler.visibility = visibilidad
    }
}
