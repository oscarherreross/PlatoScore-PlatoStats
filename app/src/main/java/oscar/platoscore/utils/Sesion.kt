package oscar.platoscore.utils

import com.google.firebase.auth.FirebaseAuth

/**
 * Utilidades de sesión. El rol (profesional o personal) es solo el modo con
 * el que se entra: una misma cuenta puede usarse en ambos, por lo que el rol
 * no se guarda en la cuenta, solo decide a qué pantalla se navega.
 */
object Sesion {

    const val ROL_PROFESIONAL = "profesional"
    const val ROL_PERSONAL = "personal"

    /** UID del usuario autenticado, o cadena vacía si no hay sesión. */
    fun uid(): String = FirebaseAuth.getInstance().currentUser?.uid ?: ""
}
