package oscar.platoscore.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import oscar.platoscore.databinding.ActivityTiradaDetailBinding
import oscar.platoscore.models.Escuadra
import oscar.platoscore.models.Tirada
import oscar.platoscore.ui.adapters.EscuadraAdapter
import oscar.platoscore.viewmodels.EscuadraViewModel
import oscar.platoscore.viewmodels.TiradaViewModel

class TiradaDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTiradaDetailBinding
    private val tiradaViewModel: TiradaViewModel by viewModels()
    private val escuadraViewModel: EscuadraViewModel by viewModels()
    private lateinit var escuadraAdapter: EscuadraAdapter

    private var tiradaId: Int = 0
    private var tirada: Tirada? = null

    companion object {
        const val EXTRA_TIRADA_ID = "tirada_id"
        const val EXTRA_ESCUADRA_ID = "escuadra_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTiradaDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tiradaId = intent.getIntExtra(EXTRA_TIRADA_ID, 0)

        setupRecyclerView()
        observeTirada()
        setupFAB()
        setupGenerarResultadosButton()
    }

    override fun onPause() {
        super.onPause()
        guardarCambiosTirada()
    }

    private fun guardarCambiosTirada() {
        tirada?.let { t ->
            val tiradaActualizada = t.copy(
                nombre = binding.etNombreTirada.text.toString(),
                precioLocal = binding.etPrecioLocal.text.toString().toFloatOrNull() ?: 0f,
                precioGeneral = binding.etPrecioGeneral.text.toString().toFloatOrNull() ?: 0f,
                precioJunior = binding.etPrecioJunior.text.toString().toFloatOrNull() ?: 0f,
                precioSenior = binding.etPrecioSenior.text.toString().toFloatOrNull() ?: 0f,
                precioDama = binding.etPrecioDama.text.toString().toFloatOrNull() ?: 0f
            )
            tiradaViewModel.updateTirada(tiradaActualizada)
        }
    }

    private fun setupRecyclerView() {
        escuadraAdapter = EscuadraAdapter(
            onClickListener = { escuadra ->
                val intent = Intent(this, EscuadraDetailActivity::class.java)
                intent.putExtra(EXTRA_TIRADA_ID, tiradaId)
                intent.putExtra(EXTRA_ESCUADRA_ID, escuadra.id)
                startActivity(intent)
            },
            onLongClickListener = { escuadra ->
                confirmarEliminarEscuadra(escuadra)
            }
        )

        binding.rvEscuadras.apply {
            adapter = escuadraAdapter
            layoutManager = LinearLayoutManager(this@TiradaDetailActivity)
            isNestedScrollingEnabled = false
        }
    }

    private fun confirmarEliminarEscuadra(escuadra: Escuadra) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar escuadra")
            .setMessage("¿Estás seguro de que quieres eliminar la Escuadra ${escuadra.numeroEscuadra}? Se eliminarán también todos sus tiradores.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                escuadraViewModel.deleteEscuadra(escuadra)
                Toast.makeText(this, "Escuadra eliminada", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun observeTirada() {
        tiradaViewModel.getTirada(tiradaId).observe(this) { tirada ->
            if (tirada == null) return@observe

            // Los campos solo se rellenan en la primera carga: si se repoblaran
            // en cada emisión (p. ej. tras guardar en onPause o al girar la
            // pantalla), se pisaría lo que el usuario esté escribiendo.
            val esPrimeraCarga = this.tirada == null
            this.tirada = tirada

            if (esPrimeraCarga) {
                binding.etNombreTirada.setText(tirada.nombre)
                binding.etPrecioLocal.setText(tirada.precioLocal.toString())
                binding.etPrecioGeneral.setText(tirada.precioGeneral.toString())
                binding.etPrecioJunior.setText(tirada.precioJunior.toString())
                binding.etPrecioSenior.setText(tirada.precioSenior.toString())
                binding.etPrecioDama.setText(tirada.precioDama.toString())
            }
        }

        escuadraViewModel.getEscuadrasByTirada(tiradaId).observe(this) { escuadras ->
            escuadraAdapter.submitList(escuadras)
        }
    }

    private fun setupFAB() {
        binding.fabAddEscuadra.setOnClickListener {
            val numeroEscuadra = (escuadraAdapter.itemCount + 1)
            val nuevaEscuadra = Escuadra(
                tiradaId = tiradaId,
                numeroEscuadra = numeroEscuadra
            )
            escuadraViewModel.insertEscuadra(nuevaEscuadra)
        }
    }

    private fun setupGenerarResultadosButton() {
        binding.btnGenerarResultados.setOnClickListener {
            guardarCambiosTirada()

            val intent = Intent(this, ResultadosActivity::class.java)
            intent.putExtra(EXTRA_TIRADA_ID, tiradaId)
            startActivity(intent)
        }
    }
}
