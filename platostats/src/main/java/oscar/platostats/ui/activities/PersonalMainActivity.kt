package oscar.platostats.ui.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import oscar.plato.core.models.FiltroTiradas
import oscar.plato.core.ui.CuentaUi
import oscar.plato.core.utils.enableEdgeToEdgeConToolbar
import oscar.platostats.R
import oscar.platostats.databinding.ActivityPersonalMainBinding
import oscar.platostats.models.PerfilUsuario
import oscar.platostats.models.ResumenUsuario
import oscar.platostats.models.TiradaPersonalConSeries
import oscar.platostats.models.acepta
import oscar.platostats.ui.FiltrosPersonales
import oscar.platostats.ui.adapters.TiradaPersonalAdapter
import oscar.platostats.utils.Extras
import oscar.platostats.viewmodels.TiradaPersonalViewModel
import oscar.plato.core.R as CoreR

/** Pantalla principal del rol personal: lista de tiradas y menú lateral de perfil. */
class PersonalMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalMainBinding
    private val viewModel: TiradaPersonalViewModel by viewModels()
    private lateinit var adapter: TiradaPersonalAdapter
    private lateinit var drawerToggle: ActionBarDrawerToggle

    private var todas: List<TiradaPersonalConSeries> = emptyList()
    private var filtro = FiltroTiradas()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeConToolbar()
        binding = ActivityPersonalMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDrawer()
        aplicarInsets()
        setupRecyclerView()
        observar()

        binding.fabAddTirada.setOnClickListener {
            startActivity(Intent(this, PersonalTiradaDetailActivity::class.java))
        }
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.tvEmail.text = FirebaseAuth.getInstance().currentUser?.email
        binding.btnEstadisticas.setOnClickListener {
            startActivity(Intent(this, EstadisticasActivity::class.java))
        }
        binding.btnCambiarPassword.setOnClickListener { CuentaUi.mostrarCambiarPassword(this) }
        binding.btnCerrarSesion.setOnClickListener { CuentaUi.cerrarSesion(this) }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
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
                FiltrosPersonales.mostrar(this, filtro) { nuevo ->
                    filtro = nuevo
                    render()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        adapter = TiradaPersonalAdapter(
            onClickListener = { item ->
                val intent = Intent(this, PersonalTiradaDetailActivity::class.java)
                intent.putExtra(Extras.TIRADA_PERSONAL_ID, item.tirada.id)
                startActivity(intent)
            },
            onLongClickListener = { item -> confirmarEliminar(item) }
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
    }

    private fun render() {
        val filtradas = todas.filter { filtro.acepta(it) }
        adapter.submitList(filtradas)
        binding.tvEmpty.visibility = if (filtradas.isEmpty()) View.VISIBLE else View.GONE
        binding.tvEmpty.setText(
            if (todas.isEmpty()) R.string.personal_vacio else CoreR.string.personal_vacio_filtro
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

    private fun confirmarEliminar(item: TiradaPersonalConSeries) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.titulo_eliminar_tirada_personal)
            .setMessage(getString(R.string.msg_eliminar_tirada_personal, item.tirada.lugar))
            .setNegativeButton(CoreR.string.accion_cancelar, null)
            .setPositiveButton(CoreR.string.accion_eliminar) { _, _ ->
                viewModel.eliminar(item.tirada)
                Toast.makeText(this, R.string.toast_tirada_personal_eliminada, Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}
