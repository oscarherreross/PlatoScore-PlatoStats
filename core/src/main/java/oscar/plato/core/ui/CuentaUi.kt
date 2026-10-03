package oscar.plato.core.ui

import android.app.Dialog
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import oscar.plato.core.R
import oscar.plato.core.databinding.DialogCambiarPasswordBinding
import oscar.plato.core.databinding.DialogEliminarCuentaBinding
import oscar.plato.core.viewmodels.CuentaViewModel
import oscar.plato.core.viewmodels.EstadoCuenta

/**
 * Acciones de cuenta de la pantalla principal de las dos apps: cerrar sesión,
 * cambiar la contraseña y eliminar la cuenta. La pantalla la crea en su onCreate.
 *
 * Los diálogos solo recogen los datos; las operaciones las hace [CuentaViewModel],
 * así que siguen su curso aunque la pantalla se recree a mitad.
 */
class CuentaUi(
    private val activity: AppCompatActivity,
    private val dialogos: DialogosRestaurables
) {

    /** Un diálogo de cuenta ya construido, para reflejar en él el estado de su operación. */
    private class Formulario<B>(val binding: B, val dialogo: AlertDialog)

    private val viewModel = ViewModelProvider(activity)[CuentaViewModel::class.java]

    private var formularioPassword: Formulario<DialogCambiarPasswordBinding>? = null
    private var formularioEliminar: Formulario<DialogEliminarCuentaBinding>? = null

    init {
        dialogos.registrar(DIALOGO_PASSWORD) { crearDialogoPassword() }
        dialogos.registrar(DIALOGO_ELIMINAR) { crearDialogoEliminar() }
        viewModel.cambioPassword.observe(activity) { reflejarCambioPassword(it) }
        viewModel.eliminacion.observe(activity) { reflejarEliminacion(it) }
    }

    /** Cierra la sesión y vuelve al inicio de sesión, vaciando la pila de pantallas. */
    fun cerrarSesion() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(activity, R.string.toast_sesion_cerrada, Toast.LENGTH_SHORT).show()
        irAlInicioDeSesion()
    }

    fun mostrarCambiarPassword() = dialogos.mostrar(DIALOGO_PASSWORD)

    fun mostrarEliminarCuenta() = dialogos.mostrar(DIALOGO_ELIMINAR)

    // ─── CAMBIAR CONTRASEÑA ───

    private fun crearDialogoPassword(): Dialog {
        val binding = DialogCambiarPasswordBinding.inflate(activity.layoutInflater)
        val dialogo = MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.accion_cambiar_password)
            .setView(binding.root)
            .setNegativeButton(R.string.accion_cancelar, null)
            .setPositiveButton(R.string.accion_guardar, null)
            .create()
        // El botón positivo se gestiona a mano para no cerrar el diálogo si hay error.
        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                cambiarPassword(binding)
            }
        }
        formularioPassword = Formulario(binding, dialogo)
        return dialogo
    }

    private fun cambiarPassword(binding: DialogCambiarPasswordBinding) {
        binding.tilActual.error = null
        binding.tilNueva.error = null
        binding.tilConfirm.error = null

        val actual = binding.etActual.text?.toString().orEmpty()
        val nueva = binding.etNueva.text?.toString().orEmpty()
        val confirm = binding.etConfirm.text?.toString().orEmpty()

        if (actual.isEmpty()) {
            binding.tilActual.error = activity.getString(R.string.password_error_actual); return
        }
        if (nueva.length < 6) {
            binding.tilNueva.error = activity.getString(R.string.login_error_password_corta); return
        }
        if (nueva != confirm) {
            binding.tilConfirm.error = activity.getString(R.string.login_error_password_no_coincide); return
        }

        viewModel.cambiarPassword(actual, nueva)
    }

    private fun reflejarCambioPassword(estado: EstadoCuenta) {
        val formulario = formularioPassword
        bloquear(formulario?.dialogo, estado == EstadoCuenta.EnCurso)
        when (estado) {
            is EstadoCuenta.Error -> {
                if (estado.enPassword && formulario != null) {
                    formulario.binding.tilActual.error =
                        activity.getString(R.string.password_error_actual_incorrecta)
                } else {
                    Toast.makeText(activity, estado.mensaje, Toast.LENGTH_LONG).show()
                }
                viewModel.cambioPasswordAtendido()
            }
            is EstadoCuenta.Hecho -> {
                Toast.makeText(activity, estado.mensaje, Toast.LENGTH_SHORT).show()
                formulario?.dialogo?.dismiss()
                viewModel.cambioPasswordAtendido()
            }
            else -> Unit
        }
    }

    // ─── ELIMINAR CUENTA ───

    private fun crearDialogoEliminar(): Dialog {
        val binding = DialogEliminarCuentaBinding.inflate(activity.layoutInflater)
        val dialogo = MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.accion_eliminar_cuenta)
            .setView(binding.root)
            .setNegativeButton(R.string.accion_cancelar, null)
            .setPositiveButton(R.string.accion_eliminar_cuenta, null)
            .create()
        // El botón positivo se gestiona a mano para no cerrar el diálogo si hay error.
        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).apply {
                setTextColor(MaterialColors.getColor(this, androidx.appcompat.R.attr.colorError))
                setOnClickListener { eliminarCuenta(binding) }
            }
        }
        formularioEliminar = Formulario(binding, dialogo)
        return dialogo
    }

    private fun eliminarCuenta(binding: DialogEliminarCuentaBinding) {
        binding.tilPassword.error = null
        val password = binding.etPassword.text?.toString().orEmpty()
        if (password.isEmpty()) {
            binding.tilPassword.error = activity.getString(R.string.password_error_vacia); return
        }
        viewModel.eliminarCuenta(password)
    }

    private fun reflejarEliminacion(estado: EstadoCuenta) {
        val formulario = formularioEliminar
        bloquear(formulario?.dialogo, estado == EstadoCuenta.EnCurso)
        when (estado) {
            is EstadoCuenta.Error -> {
                if (estado.enPassword && formulario != null) {
                    formulario.binding.tilPassword.error = activity.getString(estado.mensaje)
                } else {
                    Toast.makeText(activity, estado.mensaje, Toast.LENGTH_LONG).show()
                }
                viewModel.eliminacionAtendida()
            }
            is EstadoCuenta.Hecho -> {
                Toast.makeText(activity, estado.mensaje, Toast.LENGTH_LONG).show()
                viewModel.eliminacionAtendida()
                irAlInicioDeSesion()
            }
            else -> Unit
        }
    }

    // ─── COMÚN ───

    /** Mientras la operación está en marcha no se puede repetir ni cerrar su diálogo. */
    private fun bloquear(dialogo: AlertDialog?, enCurso: Boolean) {
        dialogo ?: return
        dialogo.setCancelable(!enCurso)
        dialogo.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = !enCurso
        dialogo.getButton(AlertDialog.BUTTON_NEGATIVE)?.isEnabled = !enCurso
    }

    private fun irAlInicioDeSesion() {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }

    private companion object {
        const val DIALOGO_PASSWORD = "cuenta_password"
        const val DIALOGO_ELIMINAR = "cuenta_eliminar"
    }
}
