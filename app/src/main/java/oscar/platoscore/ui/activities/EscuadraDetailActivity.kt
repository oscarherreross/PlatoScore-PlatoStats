package oscar.platoscore.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import oscar.platoscore.R
import oscar.platoscore.databinding.ActivityEscuadraDetailBinding
import oscar.platoscore.databinding.DialogAddTiradorBinding
import oscar.platoscore.models.Tirador
import oscar.platoscore.ui.adapters.TiradorAdapter
import oscar.platoscore.utils.Extras
import oscar.platoscore.utils.InsetsUtil
import oscar.platoscore.utils.enableEdgeToEdgeConToolbar
import oscar.platoscore.viewmodels.TiradaViewModel
import oscar.platoscore.viewmodels.TiradorViewModel

class EscuadraDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEscuadraDetailBinding

    private val tiradaViewModel: TiradaViewModel by viewModels()
    private val tiradorViewModel: TiradorViewModel by viewModels()

    private lateinit var tiradorAdapter: TiradorAdapter

    private var tiradaId: Int = 0
    private var escuadraId: Int = 0

    /** Histórico de todos los tiradores (más recientes primero) para autocompletar. */
    private var historicoTiradores: List<Tirador> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeConToolbar()
        binding = ActivityEscuadraDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        InsetsUtil.padTop(binding.toolbar)
        InsetsUtil.padBottom(binding.rvTiradores)
        InsetsUtil.addMarginBottom(binding.fabAddTirador)

        tiradaId = intent.getIntExtra(Extras.TIRADA_ID, 0)
        escuadraId = intent.getIntExtra(Extras.ESCUADRA_ID, 0)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.titulo_escuadra)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecycler()
        observeData()
        setupFab()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
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

        tiradorViewModel.getAllTiradores().observe(this) { todos ->
            historicoTiradores = todos
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
        configurarValidaciones(dialogBinding)
        configurarAutocompletado(dialogBinding)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.titulo_anadir_tirador)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.accion_cancelar, null)
            .setPositiveButton(R.string.accion_guardar) { _, _ ->
                val tirador = buildTiradorFromDialog(dialogBinding, existente = null)
                if (tirador != null) {
                    tiradorViewModel.insertTirador(tirador)
                }
            }
            .show()
    }

    // ─── DIÁLOGO: EDITAR TIRADOR ───

    private fun showEditTiradorDialog(tirador: Tirador) {
        val dialogBinding = DialogAddTiradorBinding.inflate(layoutInflater)
        configurarValidaciones(dialogBinding)

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
            .setTitle(R.string.titulo_editar_tirador)
            .setView(dialogBinding.root)
            .setNeutralButton(R.string.accion_eliminar) { _, _ ->
                tiradorViewModel.deleteTirador(tirador)
                Toast.makeText(this, R.string.toast_tirador_eliminado, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.accion_cancelar, null)
            .setPositiveButton(R.string.accion_guardar) { _, _ ->
                val tiradorActualizado = buildTiradorFromDialog(dialogBinding, existente = tirador)
                if (tiradorActualizado != null) {
                    tiradorViewModel.updateTirador(tiradorActualizado)
                }
            }
            .show()
    }

    /**
     * Sugiere nombres del histórico de tiradores mientras se escribe; al
     * elegir uno se rellenan DNI, licencia y categorías con sus últimos
     * datos (los platos rotos no: son de cada tirada).
     */
    private fun configurarAutocompletado(dialogBinding: DialogAddTiradorBinding) {
        val nombres = historicoTiradores
            .map { it.nombreApellidos }
            .distinctBy { it.lowercase() }

        dialogBinding.etNombreApellidos.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nombres)
        )

        dialogBinding.etNombreApellidos.setOnItemClickListener { _, _, _, _ ->
            val nombreElegido = dialogBinding.etNombreApellidos.text.toString()
            val anterior = historicoTiradores.firstOrNull {
                it.nombreApellidos.equals(nombreElegido, ignoreCase = true)
            } ?: return@setOnItemClickListener

            dialogBinding.etDni.setText(anterior.dni)
            dialogBinding.etNumeroLicencia.setText(anterior.numeroLicencia)
            dialogBinding.cbLocal.isChecked = anterior.esLocal
            dialogBinding.cbJunior.isChecked = anterior.esJunior
            dialogBinding.cbSenior.isChecked = anterior.esSenior
            dialogBinding.cbDama.isChecked = anterior.esDama
        }
    }

    /** Junior y Senior son categorías por edad: no pueden marcarse a la vez. */
    private fun configurarValidaciones(dialogBinding: DialogAddTiradorBinding) {
        dialogBinding.cbJunior.setOnCheckedChangeListener { _, marcado ->
            if (marcado) dialogBinding.cbSenior.isChecked = false
        }
        dialogBinding.cbSenior.setOnCheckedChangeListener { _, marcado ->
            if (marcado) dialogBinding.cbJunior.isChecked = false
        }
    }

    // ─── CONSTRUIR TIRADOR DESDE EL DIÁLOGO ───

    private fun buildTiradorFromDialog(dialogBinding: DialogAddTiradorBinding, existente: Tirador?): Tirador? {
        val nombre = dialogBinding.etNombreApellidos.text?.toString()?.trim().orEmpty()
        val dni = dialogBinding.etDni.text?.toString()?.trim().orEmpty()
        val licencia = dialogBinding.etNumeroLicencia.text?.toString()?.trim().orEmpty()
        val platosRotos = dialogBinding.etPlatosRotos.text?.toString()?.toIntOrNull() ?: 0

        if (nombre.isBlank()) {
            Toast.makeText(this, R.string.error_nombre_obligatorio, Toast.LENGTH_SHORT).show()
            return null
        }

        return Tirador(
            id = existente?.id ?: 0,
            escuadraId = escuadraId,
            nombreApellidos = nombre,
            dni = dni,
            numeroLicencia = licencia,
            platosRotos = platosRotos,
            esLocal = dialogBinding.cbLocal.isChecked,
            esJunior = dialogBinding.cbJunior.isChecked,
            esSenior = dialogBinding.cbSenior.isChecked,
            esDama = dialogBinding.cbDama.isChecked,
            // El desempate manual solo sigue valiendo si no cambian los platos
            ordenDesempate = if (existente != null && existente.platosRotos == platosRotos) {
                existente.ordenDesempate
            } else {
                0
            }
        )
    }
}
