package oscar.platoscore.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import oscar.platoscore.R
import oscar.platoscore.databinding.ActivityPersonalMainBinding
import oscar.platoscore.models.TiradaPersonalConSeries
import oscar.platoscore.ui.adapters.TiradaPersonalAdapter
import oscar.platoscore.utils.Extras
import oscar.platoscore.viewmodels.TiradaPersonalViewModel

/** Pantalla principal del rol personal: lista de tiradas registradas por el tirador. */
class PersonalMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalMainBinding
    private val viewModel: TiradaPersonalViewModel by viewModels()
    private lateinit var adapter: TiradaPersonalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observar()
        binding.fabAddTirada.setOnClickListener {
            startActivity(Intent(this, PersonalTiradaDetailActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_personal, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_estadisticas -> {
                startActivity(Intent(this, EstadisticasActivity::class.java))
                true
            }
            R.id.action_cerrar_sesion -> {
                cerrarSesion()
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
            adapter.submitList(tiradas)
            binding.tvEmpty.visibility = if (tiradas.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun confirmarEliminar(item: TiradaPersonalConSeries) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.titulo_eliminar_tirada_personal)
            .setMessage(getString(R.string.msg_eliminar_tirada_personal, item.tirada.lugar))
            .setNegativeButton(R.string.accion_cancelar, null)
            .setPositiveButton(R.string.accion_eliminar) { _, _ ->
                viewModel.eliminar(item.tirada)
                Toast.makeText(this, R.string.toast_tirada_personal_eliminada, Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun cerrarSesion() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this, R.string.toast_sesion_cerrada, Toast.LENGTH_SHORT).show()
        val intent = Intent(this, RoleSelectionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
