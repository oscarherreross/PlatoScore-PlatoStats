package oscar.platoscore.ui.activities

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import oscar.plato.core.ui.DialogosRestaurables
import oscar.plato.core.ui.exigirSesion
import oscar.plato.core.utils.InsetsUtil
import oscar.plato.core.utils.enableEdgeToEdgeConToolbar
import oscar.platoscore.R
import oscar.platoscore.databinding.ActivityEscuadraDetailBinding
import oscar.platoscore.databinding.DialogAddTiradorBinding
import oscar.platoscore.models.Tirador
import oscar.platoscore.ui.adapters.TiradorAdapter
import oscar.platoscore.utils.Extras
import oscar.platoscore.viewmodels.TiradaViewModel
import oscar.platoscore.viewmodels.TiradorViewModel
import oscar.plato.core.R as CoreR

class EscuadraDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEscuadraDetailBinding

    private val tiradaViewModel: TiradaViewModel by viewModels()
    private val tiradorViewModel: TiradorViewModel by viewModels()

    private lateinit var tiradorAdapter: TiradorAdapter

    private val dialogos = DialogosRestaurables(this)

    private var tiradaId: Int = 0
    private var escuadraId: Int = 0

    /** Histórico de todos los tiradores (más recientes primero) para autocompletar. */
    private var historicoTiradores: List<Tirador> = emptyList()

    /** Formulario de alta abierto, para darle el autocompletado cuando llegue el histórico. */
    private var formularioAlta: DialogAddTiradorBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!exigirSesion()) return
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

        // Registrado aquí, el diálogo sigue abierto si la pantalla se recrea (giro).
        dialogos.registrar(DIALOGO_TIRADOR) { args ->
            crearDialogoTirador(BundleCompat.getParcelable(args, ARG_TIRADOR, Tirador::class.java))
        }

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
            dialogos.mostrar(DIALOGO_TIRADOR, bundleOf(ARG_TIRADOR to tirador))
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
            // Un alta reabierta tras recrearse la pantalla nace antes de que llegue el histórico.
            formularioAlta?.let { configurarAutocompletado(it) }
        }
    }

    private fun setupFab() {
        binding.fabAddTirador.setOnClickListener {
            dialogos.mostrar(DIALOGO_TIRADOR)
        }
    }

    // ─── DIÁLOGO: AÑADIR O EDITAR TIRADOR ───

    /** Alta si [existente] es null; si no, edición de ese tirador. */
    private fun crearDialogoTirador(existente: Tirador?): Dialog {
        val dialogBinding = DialogAddTiradorBinding.inflate(layoutInflater)
        configurarValidaciones(dialogBinding)

        val builder = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setNegativeButton(CoreR.string.accion_cancelar, null)

        if (existente == null) {
            formularioAlta = dialogBinding
            configurarAutocompletado(dialogBinding)
            builder
                .setTitle(R.string.titulo_anadir_tirador)
                .setPositiveButton(CoreR.string.accion_guardar) { _, _ ->
                    buildTiradorFromDialog(dialogBinding, existente = null)
                        ?.let { tiradorViewModel.insertTirador(it) }
                }
        } else {
            formularioAlta = null

            // Precargar los campos con los datos actuales
            dialogBinding.etNombreApellidos.setText(existente.nombreApellidos)
            dialogBinding.etDni.setText(existente.dni)
            dialogBinding.etNumeroLicencia.setText(existente.numeroLicencia)
            dialogBinding.etPlatosRotos.setText(existente.platosRotos.toString())
            dialogBinding.cbLocal.isChecked = existente.esLocal
            dialogBinding.cbJunior.isChecked = existente.esJunior
            dialogBinding.cbSenior.isChecked = existente.esSenior
            dialogBinding.cbDama.isChecked = existente.esDama

            builder
                .setTitle(R.string.titulo_editar_tirador)
                .setNeutralButton(CoreR.string.accion_eliminar) { _, _ ->
                    tiradorViewModel.deleteTirador(existente)
                    Toast.makeText(this, R.string.toast_tirador_eliminado, Toast.LENGTH_SHORT).show()
                }
                .setPositiveButton(CoreR.string.accion_guardar) { _, _ ->
                    buildTiradorFromDialog(dialogBinding, existente)
                        ?.let { tiradorViewModel.updateTirador(it) }
                }
        }
        return builder.create()
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

    private companion object {
        const val DIALOGO_TIRADOR = "tirador"
        const val ARG_TIRADOR = "tirador"
    }
}
