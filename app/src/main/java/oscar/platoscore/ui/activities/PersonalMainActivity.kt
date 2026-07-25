package oscar.platoscore.ui.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import oscar.platoscore.R
import oscar.platoscore.databinding.ActivityPersonalMainBinding
import oscar.platoscore.databinding.DialogCambiarPasswordBinding
import oscar.platoscore.models.PerfilUsuario
import oscar.platoscore.models.ResumenUsuario
import oscar.platoscore.models.TiradaPersonalConSeries
import oscar.platoscore.ui.adapters.TiradaPersonalAdapter
import oscar.platoscore.utils.Extras
import oscar.platoscore.viewmodels.TiradaPersonalViewModel

/** Pantalla principal del rol personal: lista de tiradas y menú lateral de perfil. */
class PersonalMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalMainBinding
    private val viewModel: TiradaPersonalViewModel by viewModels()
    private lateinit var adapter: TiradaPersonalAdapter
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDrawer()
        setupRecyclerView()
        observar()

        binding.fabAddTirada.setOnClickListener {
            startActivity(Intent(this, PersonalTiradaDetailActivity::class.java))
        }
    }

    private fun setupDrawer() {
        drawerToggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, R.string.drawer_abrir, R.string.drawer_cerrar
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.tvEmail.text = FirebaseAuth.getInstance().currentUser?.email
        binding.btnEstadisticas.setOnClickListener {
            startActivity(Intent(this, EstadisticasActivity::class.java))
        }
        binding.btnCambiarPassword.setOnClickListener { mostrarCambiarPassword() }
        binding.btnCerrarSesion.setOnClickListener { cerrarSesion() }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Solo el icono de hamburguesa (home) abre/cierra el menú lateral.
        if (drawerToggle.onOptionsItemSelected(item)) return true
        return super.onOptionsItemSelected(item)
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
            mostrarResumen(PerfilUsuario.calcular(tiradas))
        }
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
            .setNegativeButton(R.string.accion_cancelar, null)
            .setPositiveButton(R.string.accion_eliminar) { _, _ ->
                viewModel.eliminar(item.tirada)
                Toast.makeText(this, R.string.toast_tirada_personal_eliminada, Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    // ─── CAMBIAR CONTRASEÑA ───

    private fun mostrarCambiarPassword() {
        val dialogBinding = DialogCambiarPasswordBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.accion_cambiar_password)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.accion_cancelar, null)
            .setPositiveButton(R.string.accion_guardar, null)
            .create()
        // El botón positivo se gestiona a mano para no cerrar el diálogo si hay error.
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                cambiarPassword(dialogBinding, dialog)
            }
        }
        dialog.show()
    }

    private fun cambiarPassword(dialogBinding: DialogCambiarPasswordBinding, dialog: AlertDialog) {
        dialogBinding.tilActual.error = null
        dialogBinding.tilNueva.error = null
        dialogBinding.tilConfirm.error = null

        val actual = dialogBinding.etActual.text?.toString().orEmpty()
        val nueva = dialogBinding.etNueva.text?.toString().orEmpty()
        val confirm = dialogBinding.etConfirm.text?.toString().orEmpty()

        if (actual.isEmpty()) {
            dialogBinding.tilActual.error = getString(R.string.password_error_actual); return
        }
        if (nueva.length < 6) {
            dialogBinding.tilNueva.error = getString(R.string.login_error_password_corta); return
        }
        if (nueva != confirm) {
            dialogBinding.tilConfirm.error = getString(R.string.login_error_password_no_coincide); return
        }

        val usuario = FirebaseAuth.getInstance().currentUser
        val email = usuario?.email
        if (usuario == null || email == null) {
            Toast.makeText(this, R.string.login_error_generico, Toast.LENGTH_LONG).show(); return
        }

        val boton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        boton.isEnabled = false

        // Reautenticación con la contraseña actual antes de cambiarla.
        val credencial = EmailAuthProvider.getCredential(email, actual)
        usuario.reauthenticate(credencial).addOnCompleteListener(this) { reauth ->
            if (!reauth.isSuccessful) {
                boton.isEnabled = true
                dialogBinding.tilActual.error = getString(R.string.password_error_actual_incorrecta)
                return@addOnCompleteListener
            }
            usuario.updatePassword(nueva).addOnCompleteListener(this) { actualizacion ->
                boton.isEnabled = true
                if (actualizacion.isSuccessful) {
                    Toast.makeText(this, R.string.toast_password_cambiada, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, R.string.login_error_generico, Toast.LENGTH_LONG).show()
                }
            }
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
}
