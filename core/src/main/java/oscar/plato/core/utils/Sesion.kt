package oscar.plato.core.utils

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import oscar.plato.core.R

/** Utilidades de sesión (Firebase Authentication). */
object Sesion {

    private var validezComprobada = false

    /** UID del usuario autenticado, o cadena vacía si no hay sesión. */
    fun uid(): String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /** ¿Hay un usuario con la sesión iniciada? */
    fun iniciada(): Boolean = FirebaseAuth.getInstance().currentUser != null

    /**
     * Comprueba con Firebase, una vez por arranque de la app, que la cuenta de la
     * sesión sigue siendo válida. Si se eliminó, se deshabilitó o cambió de contraseña
     * (por ejemplo, desde otro dispositivo), cierra la sesión; las pantallas reaccionan
     * volviendo al inicio de sesión. Sin conexión no se hace nada y se reintenta más
     * tarde: los datos son locales y la app debe seguir funcionando.
     */
    fun comprobarQueSigueSiendoValida(contexto: Context) {
        if (validezComprobada) return
        val usuario = FirebaseAuth.getInstance().currentUser ?: return
        validezComprobada = true
        usuario.reload().addOnFailureListener { error ->
            if (error is FirebaseAuthInvalidUserException) {
                Toast.makeText(contexto, R.string.toast_sesion_caducada, Toast.LENGTH_LONG).show()
                FirebaseAuth.getInstance().signOut()
            } else {
                validezComprobada = false
            }
        }
    }
}
