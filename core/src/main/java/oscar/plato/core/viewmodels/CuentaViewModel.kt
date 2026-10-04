package oscar.plato.core.viewmodels

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import oscar.plato.core.PlatoApp
import oscar.plato.core.R

/** Estado de una operación de cuenta, que tarda porque habla con Firebase. */
sealed interface EstadoCuenta {
    data object Reposo : EstadoCuenta
    data object EnCurso : EstadoCuenta

    /** Ha fallado. Con [enPassword], porque la contraseña escrita no es la de la cuenta. */
    data class Error(@param:StringRes val mensaje: Int, val enPassword: Boolean = false) : EstadoCuenta

    /** Ha terminado; [mensaje] es lo que hay que contarle al usuario. */
    data class Hecho(@param:StringRes val mensaje: Int) : EstadoCuenta
}

/**
 * Cambio de contraseña y eliminación de la cuenta. Las operaciones viven aquí, y no
 * en la pantalla, para que sigan su curso y se conozca su resultado aunque la
 * pantalla se recree a mitad (giro).
 */
class CuentaViewModel(application: Application) : AndroidViewModel(application) {

    private val _cambioPassword = MutableLiveData<EstadoCuenta>(EstadoCuenta.Reposo)
    val cambioPassword: LiveData<EstadoCuenta> = _cambioPassword

    private val _eliminacion = MutableLiveData<EstadoCuenta>(EstadoCuenta.Reposo)
    val eliminacion: LiveData<EstadoCuenta> = _eliminacion

    fun cambiarPassword(actual: String, nueva: String) {
        reautenticar(actual, _cambioPassword) { usuario ->
            usuario.updatePassword(nueva)
                .addOnSuccessListener {
                    _cambioPassword.value = EstadoCuenta.Hecho(R.string.toast_password_cambiada)
                }
                .addOnFailureListener { _cambioPassword.value = EstadoCuenta.Error(mensajeDe(it)) }
        }
    }

    /**
     * Elimina la cuenta de Firebase y, una vez eliminada, los datos que ese usuario
     * tenía guardados en este dispositivo.
     */
    fun eliminarCuenta(password: String) {
        reautenticar(password, _eliminacion) { usuario ->
            val uid = usuario.uid
            usuario.delete()
                .addOnSuccessListener { borrarDatosLocales(uid) }
                .addOnFailureListener { _eliminacion.value = EstadoCuenta.Error(mensajeDe(it)) }
        }
    }

    /** La pantalla ya ha mostrado el resultado del cambio de contraseña. */
    fun cambioPasswordAtendido() {
        _cambioPassword.value = EstadoCuenta.Reposo
    }

    /** La pantalla ya ha mostrado el resultado de la eliminación. */
    fun eliminacionAtendida() {
        _eliminacion.value = EstadoCuenta.Reposo
    }

    /**
     * Firebase solo admite las operaciones delicadas con una sesión recién
     * iniciada: se vuelve a autenticar con la contraseña antes de seguir.
     */
    private fun reautenticar(
        password: String,
        estado: MutableLiveData<EstadoCuenta>,
        despues: (FirebaseUser) -> Unit
    ) {
        if (estado.value == EstadoCuenta.EnCurso) return

        val usuario = FirebaseAuth.getInstance().currentUser
        val email = usuario?.email
        if (usuario == null || email == null) {
            estado.value = EstadoCuenta.Error(R.string.login_error_generico)
            return
        }

        estado.value = EstadoCuenta.EnCurso
        usuario.reauthenticate(EmailAuthProvider.getCredential(email, password))
            .addOnSuccessListener { despues(usuario) }
            .addOnFailureListener { error ->
                estado.value = EstadoCuenta.Error(
                    mensajeDe(error),
                    enPassword = error is FirebaseAuthInvalidCredentialsException
                )
            }
    }

    private fun borrarDatosLocales(uid: String) {
        val app = getApplication<Application>() as PlatoApp
        // La cuenta ya no existe y no hay vuelta atrás: el borrado no depende de que
        // esta pantalla siga abierta.
        alcanceDeApp.launch {
            val borrados = runCatching { app.borrarDatosDe(uid) }.isSuccess
            _eliminacion.value = EstadoCuenta.Hecho(
                if (borrados) R.string.toast_cuenta_eliminada
                else R.string.toast_cuenta_eliminada_sin_datos
            )
        }
    }

    @StringRes
    private fun mensajeDe(error: Exception): Int = when (error) {
        // Va antes: es un caso particular de credenciales no válidas.
        is FirebaseAuthWeakPasswordException -> R.string.login_error_password_corta
        is FirebaseAuthInvalidCredentialsException -> R.string.password_error_incorrecta
        is FirebaseNetworkException -> R.string.login_error_red
        else -> R.string.login_error_generico
    }

    private companion object {
        val alcanceDeApp = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }
}
