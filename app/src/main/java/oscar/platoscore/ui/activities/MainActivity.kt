package oscar.platoscore.ui.activities

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import oscar.plato.core.models.FiltroTiradas
import oscar.plato.core.ui.CuentaUi
import oscar.plato.core.ui.DialogosRestaurables
import oscar.plato.core.ui.FiltrosDialog
import oscar.plato.core.ui.abrirPoliticaPrivacidad
import oscar.plato.core.utils.Fechas
import oscar.plato.core.utils.enableEdgeToEdgeConToolbar
import oscar.platoscore.R
import oscar.platoscore.databinding.ActivityMainBinding
import oscar.platoscore.databinding.DialogAddTiradaBinding
import oscar.platoscore.models.ResumenProfesional
import oscar.platoscore.models.Tirada
import oscar.platoscore.models.TiradaConContadores
import oscar.platoscore.models.acepta
import oscar.platoscore.ui.adapters.TiradaAdapter
import oscar.platoscore.utils.Extras
import oscar.platoscore.viewmodels.TiradaViewModel
import oscar.plato.core.R as CoreR

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val tiradaViewModel: TiradaViewModel by viewModels()
    private lateinit var tiradaAdapter: TiradaAdapter
    private lateinit var drawerToggle: ActionBarDrawerToggle

    /** Con el menú lateral abierto, «atrás» lo cierra en lugar de salir de la app. */
    private val cerrarMenuConAtras = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = binding.drawerLayout.closeDrawers()
    }

    private val dialogos = DialogosRestaurables(this)
    private lateinit var cuenta: CuentaUi

    private var todasTiradas: List<TiradaConContadores> = emptyList()

    /** El filtro vive en el ViewModel para sobrevivir a la recreación de la pantalla. */
    private val filtro: FiltroTiradas
        get() = tiradaViewModel.filtro.value ?: FiltroTiradas()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeConToolbar()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registrarDialogos()
        setupDrawer()
        aplicarInsets()
        setupRecyclerView()
        observeTiradas()
        setupFAB()
    }

    /** Los diálogos registrados aquí siguen abiertos si la pantalla se recrea (giro). */
    private fun registrarDialogos() {
        cuenta = CuentaUi(this, dialogos)
        FiltrosDialog.registrar(this, dialogos) { nuevo -> tiradaViewModel.filtro.value = nuevo }
        dialogos.registrar(DIALOGO_NUEVA_TIRADA) { args -> crearDialogoNuevaTirada(args) }
        dialogos.registrar(DIALOGO_ELIMINAR_TIRADA) { args -> crearDialogoEliminarTirada(args) }
    }

    /**
     * Reparte los insets del sistema (edge-to-edge). El listener va en el
     * DrawerLayout porque este intercepta los insets antes que sus hijos.
     */
    private fun aplicarInsets() {
        val fabBase = (binding.fabAddTirada.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
        val listaBase = binding.rvTiradas.paddingBottom
        val cabeceraBase = binding.drawerHeader.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.drawerLayout) { _, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.updatePadding(top = barras.top)
            binding.rvTiradas.updatePadding(bottom = listaBase + barras.bottom)
            binding.fabAddTirada.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = fabBase + barras.bottom
            }
            binding.drawerHeader.updatePadding(top = cabeceraBase + barras.top)
            binding.drawerContainer.updatePadding(bottom = barras.bottom)
            insets
        }
    }

    private fun setupDrawer() {
        setSupportActionBar(binding.toolbar)
        drawerToggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, CoreR.string.drawer_abrir, CoreR.string.drawer_cerrar
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        onBackPressedDispatcher.addCallback(this, cerrarMenuConAtras)
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                cerrarMenuConAtras.isEnabled = slideOffset > 0f
            }
        })
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.tvEmail.text = FirebaseAuth.getInstance().currentUser?.email
        binding.btnCambiarPassword.setOnClickListener { cuenta.mostrarCambiarPassword() }
        binding.btnCerrarSesion.setOnClickListener { cuenta.cerrarSesion() }
        binding.btnPrivacidad.setOnClickListener { abrirPoliticaPrivacidad() }
        binding.btnEliminarCuenta.setOnClickListener { cuenta.mostrarEliminarCuenta() }

        tiradaViewModel.resumenProfesional.observe(this) { resumen ->
            mostrarResumen(resumen)
        }
    }

    private fun mostrarResumen(resumen: ResumenProfesional) {
        binding.tvNumTiradas.text =
            getString(R.string.drawer_prof_num_tiradas, resumen.numTiradas)
        binding.tvEscuadras.text =
            getString(R.string.drawer_prof_escuadras, resumen.totalEscuadras)
        binding.tvTiradores.text =
            getString(R.string.drawer_prof_tiradores, resumen.totalTiradores)
        binding.tvRecaudacion.text =
            getString(R.string.drawer_prof_recaudacion, "%.2f".format(resumen.recaudacion))
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
        // Al recrearse la pantalla con el menú abierto, este vuelve sin avisar a nadie.
        cerrarMenuConAtras.isEnabled = binding.drawerLayout.isDrawerOpen(GravityCompat.START)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(CoreR.menu.menu_filtro, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // El icono de hamburguesa (home) abre/cierra el menú lateral.
        if (drawerToggle.onOptionsItemSelected(item)) return true
        return when (item.itemId) {
            CoreR.id.action_filtros -> {
                FiltrosDialog.mostrar(dialogos, filtro)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        tiradaAdapter = TiradaAdapter(
            onClickListener = { tirada ->
                val intent = Intent(this, TiradaDetailActivity::class.java)
                intent.putExtra(Extras.TIRADA_ID, tirada.id)
                startActivity(intent)
            },
            onLongClickListener = { tirada ->
                dialogos.mostrar(DIALOGO_ELIMINAR_TIRADA, bundleOf(ARG_TIRADA to tirada))
            }
        )
        binding.rvTiradas.adapter = tiradaAdapter
        binding.rvTiradas.layoutManager = LinearLayoutManager(this)
    }

    private fun crearDialogoEliminarTirada(args: Bundle): Dialog {
        val tirada = checkNotNull(BundleCompat.getParcelable(args, ARG_TIRADA, Tirada::class.java))
        return MaterialAlertDialogBuilder(this)
            .setTitle(R.string.titulo_eliminar_tirada)
            .setMessage(getString(R.string.msg_eliminar_tirada, tirada.nombre))
            .setNegativeButton(CoreR.string.accion_cancelar, null)
            .setPositiveButton(CoreR.string.accion_eliminar) { _, _ ->
                tiradaViewModel.deleteTirada(tirada)
                Toast.makeText(this, R.string.toast_tirada_eliminada, Toast.LENGTH_SHORT).show()
            }
            .create()
    }

    private fun observeTiradas() {
        tiradaViewModel.allTiradas.observe(this) { tiradas ->
            todasTiradas = tiradas
            render()
        }
        tiradaViewModel.filtro.observe(this) { render() }
    }

    private fun render() {
        val filtradas = todasTiradas.filter { filtro.acepta(it) }
        tiradaAdapter.submitList(filtradas)
        binding.tvEmpty.visibility =
            if (filtradas.isEmpty() && todasTiradas.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupFAB() {
        binding.fabAddTirada.setOnClickListener {
            dialogos.mostrar(DIALOGO_NUEVA_TIRADA)
        }
    }

    private fun crearDialogoNuevaTirada(args: Bundle): Dialog {
        val dialogBinding = DialogAddTiradaBinding.inflate(layoutInflater)

        // La fecha elegida se anota en los argumentos, para conservarla si la
        // pantalla se recrea con el diálogo abierto.
        var fechaIso = args.getString(ARG_FECHA) ?: Fechas.hoyIso()
        dialogBinding.etFechaTirada.setText(Fechas.mostrar(fechaIso))
        dialogBinding.etFechaTirada.setOnClickListener {
            val (anio, mes, dia) = Fechas.partesIso(fechaIso)
            dialogos.mostrarDePaso(DatePickerDialog(this, { _, a, m, d ->
                fechaIso = Fechas.aIso(a, m + 1, d)
                args.putString(ARG_FECHA, fechaIso)
                dialogBinding.etFechaTirada.setText(Fechas.mostrar(fechaIso))
            }, anio, mes - 1, dia))
        }

        return MaterialAlertDialogBuilder(this)
            .setTitle(R.string.titulo_nueva_tirada)
            .setView(dialogBinding.root)
            .setNegativeButton(CoreR.string.accion_cancelar, null)
            .setPositiveButton(R.string.accion_crear) { _, _ ->
                val nombre = dialogBinding.etNombreTirada.text?.toString()?.trim().orEmpty()
                if (nombre.isBlank()) {
                    Toast.makeText(this, R.string.error_nombre_obligatorio, Toast.LENGTH_SHORT).show()
                } else {
                    tiradaViewModel.insertTirada(Tirada(nombre = nombre, fecha = fechaIso))
                }
            }
            .create()
    }

    private companion object {
        const val DIALOGO_NUEVA_TIRADA = "nueva_tirada"
        const val DIALOGO_ELIMINAR_TIRADA = "eliminar_tirada"
        const val ARG_FECHA = "fecha"
        const val ARG_TIRADA = "tirada"
    }
}
