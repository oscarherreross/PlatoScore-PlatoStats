package oscar.platoscore.ui.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import oscar.platoscore.R
import oscar.platoscore.databinding.ActivityMainBinding
import oscar.platoscore.databinding.DialogAddTiradaBinding
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cerrar_sesion -> {
                cerrarSesion()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun cerrarSesion() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this, R.string.toast_sesion_cerrada, Toast.LENGTH_SHORT).show()
        val intent = Intent(this, RoleSelectionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
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
            .setTitle(R.string.titulo_eliminar_tirada)
            .setMessage(getString(R.string.msg_eliminar_tirada, tirada.nombre))
            .setNegativeButton(R.string.accion_cancelar, null)
            .setPositiveButton(R.string.accion_eliminar) { _, _ ->
                tiradaViewModel.deleteTirada(tirada)
                Toast.makeText(this, R.string.toast_tirada_eliminada, Toast.LENGTH_SHORT).show()
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
            showNuevaTiradaDialog()
        }
    }

    private fun showNuevaTiradaDialog() {
        val dialogBinding = DialogAddTiradaBinding.inflate(layoutInflater)

        var fechaIso = Fechas.hoyIso()
        dialogBinding.etFechaTirada.setText(Fechas.mostrar(fechaIso))
        dialogBinding.etFechaTirada.setOnClickListener {
            val (anio, mes, dia) = Fechas.partesIso(fechaIso)
            DatePickerDialog(this, { _, a, m, d ->
                fechaIso = Fechas.aIso(a, m + 1, d)
                dialogBinding.etFechaTirada.setText(Fechas.mostrar(fechaIso))
            }, anio, mes - 1, dia).show()
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.titulo_nueva_tirada)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.accion_cancelar, null)
            .setPositiveButton(R.string.accion_crear) { _, _ ->
                val nombre = dialogBinding.etNombreTirada.text?.toString()?.trim().orEmpty()
                if (nombre.isBlank()) {
                    Toast.makeText(this, R.string.error_nombre_obligatorio, Toast.LENGTH_SHORT).show()
                } else {
                    tiradaViewModel.insertTirada(Tirada(nombre = nombre, fecha = fechaIso))
                }
            }
            .show()
    }
}
