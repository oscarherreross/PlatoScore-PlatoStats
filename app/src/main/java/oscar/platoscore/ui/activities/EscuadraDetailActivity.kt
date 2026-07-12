package oscar.platoscore.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import oscar.platoscore.databinding.ActivityEscuadraDetailBinding
import oscar.platoscore.databinding.DialogAddTiradorBinding
import oscar.platoscore.models.Tirador
import oscar.platoscore.ui.adapters.TiradorAdapter
import oscar.platoscore.viewmodels.TiradaViewModel
import oscar.platoscore.viewmodels.TiradorViewModel

class EscuadraDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEscuadraDetailBinding

    private val tiradaViewModel: TiradaViewModel by viewModels()
    private val tiradorViewModel: TiradorViewModel by viewModels()

    private lateinit var tiradorAdapter: TiradorAdapter

    private var tiradaId: Int = 0
    private var escuadraId: Int = 0

    companion object {
        const val EXTRA_TIRADA_ID = "tirada_id"
        const val EXTRA_ESCUADRA_ID = "escuadra_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEscuadraDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tiradaId = intent.getIntExtra(EXTRA_TIRADA_ID, 0)
        escuadraId = intent.getIntExtra(EXTRA_ESCUADRA_ID, 0)

        supportActionBar?.title = "Escuadra"

        setupRecycler()
        observeData()
        setupFab()
    }

    private fun setupRecycler() {
        tiradorAdapter = TiradorAdapter { tirador ->
            showEditTiradorDialog(tirador)
        }

        binding.rvTiradores.apply {
            adapter = tiradorAdapter
            layoutManager = LinearLayoutManager(this@EscuadraDetailActivity)
            setHasFixedSize(true)
        }
    }

    private fun observeData() {
        tiradaViewModel.getTirada(tiradaId).observe(this) { t ->
            tiradorAdapter.tirada = t
        }

        tiradorViewModel.getTiradores(escuadraId).observe(this) { tiradores ->
            tiradorAdapter.submitList(tiradores)
            binding.tvEmpty.visibility = if (tiradores.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupFab() {
        binding.fabAddTirador.setOnClickListener {
            showAddTiradorDialog()
        }
    }

    // ─── DIÁLOGO: AÑADIR TIRADOR ───

    private fun showAddTiradorDialog() {
        val dialogBinding = DialogAddTiradorBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(this)
            .setTitle("Añadir tirador")
            .setView(dialogBinding.root)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar") { _, _ ->
                val tirador = buildTiradorFromDialog(dialogBinding, existingId = 0)
                if (tirador != null) {
                    tiradorViewModel.insertTirador(tirador)
                }
            }
            .show()
    }

    // ─── DIÁLOGO: EDITAR TIRADOR ───

    private fun showEditTiradorDialog(tirador: Tirador) {
        val dialogBinding = DialogAddTiradorBinding.inflate(layoutInflater)

        // Precargar los campos con los datos actuales
        dialogBinding.etNombreApellidos.setText(tirador.nombreApellidos)
        dialogBinding.etDni.setText(tirador.dni)
        dialogBinding.etNumeroLicencia.setText(tirador.numeroLicencia)
        dialogBinding.etPlatosRotos.setText(tirador.platosRotos.toString())
        dialogBinding.cbLocal.isChecked = tirador.esLocal
        dialogBinding.cbJunior.isChecked = tirador.esJunior
        dialogBinding.cbSenior.isChecked = tirador.esSenior
        dialogBinding.cbDama.isChecked = tirador.esDama

        MaterialAlertDialogBuilder(this)
            .setTitle("Editar tirador")
            .setView(dialogBinding.root)
            .setNeutralButton("Eliminar") { _, _ ->
                tiradorViewModel.deleteTirador(tirador)
                Toast.makeText(this, "Tirador eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar") { _, _ ->
                val tiradorActualizado = buildTiradorFromDialog(dialogBinding, existingId = tirador.id)
                if (tiradorActualizado != null) {
                    tiradorViewModel.updateTirador(tiradorActualizado)
                }
            }
            .show()
    }

    // ─── CONSTRUIR TIRADOR DESDE EL DIÁLOGO ───

    private fun buildTiradorFromDialog(dialogBinding: DialogAddTiradorBinding, existingId: Int): Tirador? {
        val nombre = dialogBinding.etNombreApellidos.text?.toString()?.trim().orEmpty()
        val dni = dialogBinding.etDni.text?.toString()?.trim().orEmpty()
        val licencia = dialogBinding.etNumeroLicencia.text?.toString()?.trim().orEmpty()
        val platosRotos = dialogBinding.etPlatosRotos.text?.toString()?.toIntOrNull() ?: 0

        if (nombre.isBlank()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return null
        }

        return Tirador(
            id = existingId,
            escuadraId = escuadraId,
            nombreApellidos = nombre,
            dni = dni,
            numeroLicencia = licencia,
            platosRotos = platosRotos,
            esLocal = dialogBinding.cbLocal.isChecked,
            esJunior = dialogBinding.cbJunior.isChecked,
            esSenior = dialogBinding.cbSenior.isChecked,
            esDama = dialogBinding.cbDama.isChecked
        )
    }
}
