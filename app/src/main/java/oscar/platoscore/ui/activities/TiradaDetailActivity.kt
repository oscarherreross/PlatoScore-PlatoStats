package oscar.platoscore.ui.activities

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import oscar.plato.core.ui.DialogosRestaurables
import oscar.plato.core.ui.exigirSesion
import oscar.plato.core.utils.Fechas
import oscar.plato.core.utils.InsetsUtil
import oscar.plato.core.utils.enableEdgeToEdgeConToolbar
import oscar.platoscore.R
import oscar.platoscore.databinding.ActivityTiradaDetailBinding
import oscar.platoscore.models.Escuadra
import oscar.platoscore.models.Tirada
import oscar.platoscore.ui.adapters.EscuadraAdapter
import oscar.platoscore.utils.Extras
import oscar.platoscore.viewmodels.EscuadraViewModel
import oscar.platoscore.viewmodels.TiradaViewModel
import oscar.plato.core.R as CoreR

class TiradaDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTiradaDetailBinding
    private val tiradaViewModel: TiradaViewModel by viewModels()
    private val escuadraViewModel: EscuadraViewModel by viewModels()
    private lateinit var escuadraAdapter: EscuadraAdapter

    private val dialogos = DialogosRestaurables(this)

    private var tiradaId: Int = 0
    private var tirada: Tirada? = null

    /** Fecha en formato ISO elegida con el DatePicker (el EditText muestra dd/MM/yyyy). */
    private var fechaIso: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!exigirSesion()) return
        enableEdgeToEdgeConToolbar()
        binding = ActivityTiradaDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        InsetsUtil.padTop(binding.toolbar)
        InsetsUtil.padBottom(binding.bottomBar)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setTitle(R.string.titulo_detalle_tirada)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tiradaId = intent.getIntExtra(Extras.TIRADA_ID, 0)

        // Registrados aquí, los diálogos siguen abiertos si la pantalla se recrea (giro).
        dialogos.registrar(DIALOGO_FECHA) { crearSelectorFecha() }
        dialogos.registrar(DIALOGO_ELIMINAR_ESCUADRA) { args -> crearDialogoEliminarEscuadra(args) }

        setupRecyclerView()
        observeTirada()
        setupFechaPicker()
        setupFAB()
        setupGenerarResultadosButton()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupFechaPicker() {
        binding.etFechaTirada.setOnClickListener { dialogos.mostrar(DIALOGO_FECHA) }
    }

    private fun crearSelectorFecha(): Dialog {
        val (anio, mes, dia) = Fechas.partesIso(fechaIso)
        return DatePickerDialog(this, { _, a, m, d ->
            fechaIso = Fechas.aIso(a, m + 1, d)
            binding.etFechaTirada.setText(Fechas.mostrar(fechaIso))
        }, anio, mes - 1, dia)
    }

    override fun onPause() {
        super.onPause()
        guardarCambiosTirada()
    }

    private fun guardarCambiosTirada() {
        tirada?.let { t ->
            val tiradaActualizada = t.copy(
                nombre = binding.etNombreTirada.text.toString(),
                fecha = fechaIso.ifBlank { t.fecha },
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
                intent.putExtra(Extras.TIRADA_ID, tiradaId)
                intent.putExtra(Extras.ESCUADRA_ID, escuadra.id)
                startActivity(intent)
            },
            onLongClickListener = { escuadra ->
                dialogos.mostrar(DIALOGO_ELIMINAR_ESCUADRA, bundleOf(ARG_ESCUADRA to escuadra))
            }
        )

        binding.rvEscuadras.apply {
            adapter = escuadraAdapter
            layoutManager = LinearLayoutManager(this@TiradaDetailActivity)
            isNestedScrollingEnabled = false
        }
    }

    private fun crearDialogoEliminarEscuadra(args: Bundle): Dialog {
        val escuadra = checkNotNull(BundleCompat.getParcelable(args, ARG_ESCUADRA, Escuadra::class.java))
        return MaterialAlertDialogBuilder(this)
            .setTitle(R.string.titulo_eliminar_escuadra)
            .setMessage(getString(R.string.msg_eliminar_escuadra, escuadra.numeroEscuadra))
            .setNegativeButton(CoreR.string.accion_cancelar, null)
            .setPositiveButton(CoreR.string.accion_eliminar) { _, _ ->
                escuadraViewModel.deleteEscuadra(escuadra)
                Toast.makeText(this, R.string.toast_escuadra_eliminada, Toast.LENGTH_SHORT).show()
            }
            .create()
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
                fechaIso = tirada.fecha
                binding.etNombreTirada.setText(tirada.nombre)
                binding.etFechaTirada.setText(Fechas.mostrar(tirada.fecha))
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
            escuadraViewModel.crearEscuadra(tiradaId)
        }
    }

    private fun setupGenerarResultadosButton() {
        binding.btnGenerarResultados.setOnClickListener {
            guardarCambiosTirada()

            val intent = Intent(this, ResultadosActivity::class.java)
            intent.putExtra(Extras.TIRADA_ID, tiradaId)
            startActivity(intent)
        }
    }

    private companion object {
        const val DIALOGO_FECHA = "fecha"
        const val DIALOGO_ELIMINAR_ESCUADRA = "eliminar_escuadra"
        const val ARG_ESCUADRA = "escuadra"
    }
}
