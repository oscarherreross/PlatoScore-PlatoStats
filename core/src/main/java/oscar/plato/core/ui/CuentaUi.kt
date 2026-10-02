package oscar.plato.core.ui

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import oscar.plato.core.R
import oscar.plato.core.databinding.DialogCambiarPasswordBinding

/**
 * Acciones de cuenta compartidas por las pantallas principales de las dos apps:
 * cerrar sesión y cambiar la contraseña.
 */
object CuentaUi {

    /** Cierra la sesión y vuelve al inicio de sesión, vaciando la pila de pantallas. */
    fun cerrarSesion(activity: AppCompatActivity) {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(activity, R.string.toast_sesion_cerrada, Toast.LENGTH_SHORT).show()
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }

    fun mostrarCambiarPassword(activity: AppCompatActivity) {
        val dialogBinding = DialogCambiarPasswordBinding.inflate(activity.layoutInflater)
        val dialog = MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.accion_cambiar_password)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.accion_cancelar, null)
            .setPositiveButton(R.string.accion_guardar, null)
            .create()
        // El botón positivo se gestiona a mano para no cerrar el diálogo si hay error.
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                cambiarPassword(activity, dialogBinding, dialog)
            }
        }
        dialog.show()
    }

    private fun cambiarPassword(
        activity: AppCompatActivity,
        dialogBinding: DialogCambiarPasswordBinding,
        dialog: AlertDialog
    ) {
        dialogBinding.tilActual.error = null
        dialogBinding.tilNueva.error = null
        dialogBinding.tilConfirm.error = null

        val actual = dialogBinding.etActual.text?.toString().orEmpty()
        val nueva = dialogBinding.etNueva.text?.toString().orEmpty()
        val confirm = dialogBinding.etConfirm.text?.toString().orEmpty()

        if (actual.isEmpty()) {
            dialogBinding.tilActual.error = activity.getString(R.string.password_error_actual); return
        }
        if (nueva.length < 6) {
            dialogBinding.tilNueva.error = activity.getString(R.string.login_error_password_corta); return
        }
        if (nueva != confirm) {
            dialogBinding.tilConfirm.error = activity.getString(R.string.login_error_password_no_coincide); return
        }

        val usuario = FirebaseAuth.getInstance().currentUser
        val email = usuario?.email
        if (usuario == null || email == null) {
            Toast.makeText(activity, R.string.login_error_generico, Toast.LENGTH_LONG).show(); return
        }

        val boton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        boton.isEnabled = false

        // Reautenticación con la contraseña actual antes de cambiarla.
        val credencial = EmailAuthProvider.getCredential(email, actual)
        usuario.reauthenticate(credencial).addOnCompleteListener(activity) { reauth ->
            if (!reauth.isSuccessful) {
                boton.isEnabled = true
                dialogBinding.tilActual.error =
                    activity.getString(R.string.password_error_actual_incorrecta)
                return@addOnCompleteListener
            }
            usuario.updatePassword(nueva).addOnCompleteListener(activity) { actualizacion ->
                boton.isEnabled = true
                if (actualizacion.isSuccessful) {
                    Toast.makeText(activity, R.string.toast_password_cambiada, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(activity, R.string.login_error_generico, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
