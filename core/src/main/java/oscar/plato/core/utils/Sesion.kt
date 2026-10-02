package oscar.plato.core.utils

import com.google.firebase.auth.FirebaseAuth

/** Utilidades de sesión (Firebase Authentication). */
object Sesion {

    /** UID del usuario autenticado, o cadena vacía si no hay sesión. */
    fun uid(): String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /** ¿Hay un usuario con la sesión iniciada? */
    fun iniciada(): Boolean = FirebaseAuth.getInstance().currentUser != null
}
