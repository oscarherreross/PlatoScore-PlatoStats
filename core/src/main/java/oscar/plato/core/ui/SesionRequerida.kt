package oscar.plato.core.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import oscar.plato.core.utils.Sesion

/**
 * Exige una sesión iniciada. Las pantallas posteriores al inicio de sesión lo llaman
 * al principio de su onCreate y, si devuelve false, salen sin hacer nada más: ya van
 * camino del inicio de sesión. Sin esta comprobación la pantalla trabajaría con un
 * usuario vacío, que en PlatoScore enseñaría las tiradas sin dueño.
 *
 * Mientras la pantalla está a la vista vigila además que la sesión no se pierda, y
 * pide comprobar que la cuenta sigue existiendo ([Sesion.comprobarQueSigueSiendoValida]).
 */
fun ComponentActivity.exigirSesion(): Boolean {
    val auth = FirebaseAuth.getInstance()
    if (auth.currentUser == null) {
        irAlInicioDeSesion()
        return false
    }

    val vigilante = FirebaseAuth.AuthStateListener {
        if (it.currentUser == null) irAlInicioDeSesion()
    }
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) = auth.addAuthStateListener(vigilante)
        override fun onStop(owner: LifecycleOwner) = auth.removeAuthStateListener(vigilante)
    })

    Sesion.comprobarQueSigueSiendoValida(applicationContext)
    return true
}

/** Vuelve al inicio de sesión vaciando la pila de pantallas. */
fun Activity.irAlInicioDeSesion() {
    // Cerrar la sesión a mano y perderla llegan aquí casi a la vez: basta una vez.
    if (isFinishing) return
    val intent = Intent(this, LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
    finish()
}
