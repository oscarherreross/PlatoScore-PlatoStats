package oscar.platostats.ui.activities

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
import oscar.plato.core.ui.abrirPoliticaPrivacidad
import oscar.plato.core.utils.enableEdgeToEdgeConToolbar
import oscar.platostats.R
import oscar.platostats.databinding.ActivityMainBinding
import oscar.platostats.models.PerfilUsuario
import oscar.platostats.models.ResumenUsuario
import oscar.platostats.models.Tirada
import oscar.platostats.models.TiradaConSeries
import oscar.platostats.models.acepta
import oscar.platostats.ui.Filtros
import oscar.platostats.ui.adapters.TiradaAdapter
import oscar.platostats.utils.Extras
import oscar.platostats.viewmodels.TiradaViewModel
import oscar.plato.core.R as CoreR

/** Pantalla principal de PlatoStats: lista de tiradas y menú lateral de perfil. */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: TiradaViewModel by viewModels()
    private lateinit var adapter: TiradaAdapter
    private lateinit var drawerToggle: ActionBarDrawerToggle

    /** Con el menú lateral abierto, «atrás» lo cierra en lugar de salir de la app. */
    private val cerrarMenuConAtras = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = binding.drawerLayout.closeDrawers()
    }

    private val dialogos = DialogosRestaurables(this)
    private lateinit var cuenta: CuentaUi

    private var todas: List<TiradaConSeries> = emptyList()

    /** El filtro vive en el ViewModel para sobrevivir a la recreación de la pantalla. */
    private val filtro: FiltroTiradas
        get() = viewModel.filtro.value ?: FiltroTiradas()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeConToolbar()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registrarDialogos()
        setupDrawer()
        aplicarInsets()
        setupRecyclerView()
        observar()

        binding.fabAddTirada.setOnClickListener {
            startActivity(Intent(this, TiradaDetailActivity::class.java))
        }
    }

    /** Los diálogos registrados aquí siguen abiertos si la pantalla se recrea (giro). */
    private fun registrarDialogos() {
        cuenta = CuentaUi(this, dialogos)
        Filtros.registrar(this, dialogos) { nuevo -> viewModel.filtro.value = nuevo }
        dialogos.registrar(DIALOGO_ELIMINAR_TIRADA) { args -> crearDialogoEliminar(args) }
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
        binding.btnEstadisticas.setOnClickListener {
            startActivity(Intent(this, EstadisticasActivity::class.java))
        }
        binding.btnCambiarPassword.setOnClickListener { cuenta.mostrarCambiarPassword() }
        binding.btnCerrarSesion.setOnClickListener { cuenta.cerrarSesion() }
        binding.btnPrivacidad.setOnClickListener { abrirPoliticaPrivacidad() }
        binding.btnEliminarCuenta.setOnClickListener { cuenta.mostrarEliminarCuenta() }
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
                Filtros.mostrar(dialogos, filtro)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        adapter = TiradaAdapter(
            onClickListener = { item ->
                val intent = Intent(this, TiradaDetailActivity::class.java)
                intent.putExtra(Extras.TIRADA_ID, item.tirada.id)
                startActivity(intent)
            },
            onLongClickListener = { item ->
                dialogos.mostrar(DIALOGO_ELIMINAR_TIRADA, bundleOf(ARG_TIRADA to item.tirada))
            }
        )
        binding.rvTiradas.adapter = adapter
        binding.rvTiradas.layoutManager = LinearLayoutManager(this)
    }

    private fun observar() {
        viewModel.todas.observe(this) { tiradas ->
            todas = tiradas
            // El resumen del perfil (menú lateral) es global, sin filtro de fechas.
            mostrarResumen(PerfilUsuario.calcular(tiradas))
            render()
        }
        viewModel.filtro.observe(this) { render() }
    }

    private fun render() {
        val filtradas = todas.filter { filtro.acepta(it) }
        adapter.submitList(filtradas)
        binding.tvEmpty.visibility = if (filtradas.isEmpty()) View.VISIBLE else View.GONE
        binding.tvEmpty.setText(
            if (todas.isEmpty()) R.string.lista_vacia else CoreR.string.lista_vacia_filtro
        )
    }

    private fun mostrarResumen(resumen: ResumenUsuario) {
        binding.tvTotalPlatos.text =
            getString(R.string.drawer_total_platos, resumen.totalPlatos)
        binding.tvTotalTiros.text =
            getString(R.string.drawer_total_tiros, resumen.totalTiros)
        binding.tvPctEntrenamiento.text =
            getString(R.string.drawer_pct_entrenamiento, formatoPorcentaje(resumen.porcentajeEntrenamiento))
        binding.tvPctCompeticion.text =
            getString(R.string.drawer_pct_competicion, formatoPorcentaje(resumen.porcentajeCompeticion))
        binding.tvPctGeneral.text =
            getString(R.string.drawer_pct_general, formatoPorcentaje(resumen.porcentajeGeneral))
    }

    private fun formatoPorcentaje(valor: Float?): String =
        if (valor == null) getString(R.string.valor_sin_datos) else "%.1f %%".format(valor)

    private fun crearDialogoEliminar(args: Bundle): Dialog {
        val tirada = checkNotNull(BundleCompat.getParcelable(args, ARG_TIRADA, Tirada::class.java))
        return MaterialAlertDialogBuilder(this)
            .setTitle(R.string.titulo_eliminar_tirada)
            .setMessage(getString(R.string.msg_eliminar_tirada, tirada.lugar))
            .setNegativeButton(CoreR.string.accion_cancelar, null)
            .setPositiveButton(CoreR.string.accion_eliminar) { _, _ ->
                viewModel.eliminar(tirada)
                Toast.makeText(this, R.string.toast_tirada_eliminada, Toast.LENGTH_SHORT).show()
            }
            .create()
    }

    private companion object {
        const val DIALOGO_ELIMINAR_TIRADA = "eliminar_tirada"
        const val ARG_TIRADA = "tirada"
    }
}
