package oscar.platoscore.utils

import com.google.firebase.auth.FirebaseAuth

/**
 * El rol de la cuenta (profesional o personal) se guarda en el campo
 * displayName del usuario de Firebase. Firebase impide dos cuentas con el
 * mismo correo, así que un correo queda ligado para siempre a un solo rol:
 * para usar la app en el otro rol hay que crear una cuenta distinta.
 */
object Sesion {

    const val ROL_PROFESIONAL = "profesional"
    const val ROL_PERSONAL = "personal"

    /** UID del usuario autenticado, o cadena vacía si no hay sesión. */
    fun uid(): String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /** Rol de la cuenta autenticada (displayName), o null si no está definido. */
    fun rolActual(): String? = FirebaseAuth.getInstance().currentUser?.displayName
}
