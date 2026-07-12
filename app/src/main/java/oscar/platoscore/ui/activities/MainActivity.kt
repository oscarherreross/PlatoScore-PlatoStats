package oscar.platoscore.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import oscar.platoscore.databinding.ActivityMainBinding
import oscar.platoscore.models.Tirada
import oscar.platoscore.ui.adapters.TiradaAdapter
import oscar.platoscore.utils.Extras
import oscar.platoscore.utils.Fechas
import oscar.platoscore.viewmodels.TiradaViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val tiradaViewModel: TiradaViewModel by viewModels()
    private lateinit var tiradaAdapter: TiradaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeTiradas()
        setupFAB()
    }

    private fun setupRecyclerView() {
        tiradaAdapter = TiradaAdapter(
            onClickListener = { tirada ->
                val intent = Intent(this, TiradaDetailActivity::class.java)
                intent.putExtra(Extras.TIRADA_ID, tirada.id)
                startActivity(intent)
            },
            onLongClickListener = { tirada ->
                confirmarEliminarTirada(tirada)
            }
        )
        binding.rvTiradas.adapter = tiradaAdapter
        binding.rvTiradas.layoutManager = LinearLayoutManager(this)
    }

    private fun confirmarEliminarTirada(tirada: Tirada) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar tirada")
            .setMessage("¿Estás seguro de que quieres eliminar \"${tirada.nombre}\"? Se eliminarán también todas sus escuadras y tiradores.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                tiradaViewModel.deleteTirada(tirada)
                Toast.makeText(this, "Tirada eliminada", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun observeTiradas() {
        tiradaViewModel.allTiradas.observe(this) { tiradas ->
            tiradaAdapter.submitList(tiradas)
        }
    }

    private fun setupFAB() {
        binding.fabAddTirada.setOnClickListener {
            val nuevaTirada = Tirada(
                nombre = "Nueva Tirada",
                fecha = Fechas.hoyIso()
            )
            tiradaViewModel.insertTirada(nuevaTirada)
        }
    }
}
