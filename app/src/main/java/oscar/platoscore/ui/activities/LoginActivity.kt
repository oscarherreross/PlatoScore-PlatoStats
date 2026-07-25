package oscar.platoscore.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import oscar.platoscore.R
import oscar.platoscore.databinding.ActivityLoginBinding
import oscar.platoscore.utils.Extras
import oscar.platoscore.utils.Sesion

/**
 * Inicio de sesión con Firebase Authentication (correo y contraseña) para un
 * rol concreto (profesional o personal), recibido en [Extras.ROL]. El rol se
 * guarda en el displayName de la cuenta: al iniciar sesión se comprueba que
 * coincide, de modo que unas credenciales de un rol no sirven para el otro.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var rol: String
    private var modoRegistro = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rol = intent.getStringExtra(Extras.ROL) ?: Sesion.ROL_PROFESIONAL
        auth = FirebaseAuth.getInstance()

        val usuario = auth.currentUser
        if (usuario != null) {
            if (usuario.displayName == rol) {
                irAPrincipal()
                return
            }
            // Había una sesión de otro rol: se cierra para poder entrar en este.
            auth.signOut()
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAccion.setOnClickListener { enviar() }
        binding.tvCambiarModo.setOnClickListener { cambiarModo() }
        binding.tvOlvidePassword.setOnClickListener { recuperarPassword() }
        actualizarModo()
    }

    private fun cambiarModo() {
        modoRegistro = !modoRegistro
        limpiarErrores()
        actualizarModo()
    }

    private fun actualizarModo() {
        binding.tilPasswordConfirm.visibility = if (modoRegistro) View.VISIBLE else View.GONE
        binding.tvOlvidePassword.visibility = if (modoRegistro) View.GONE else View.VISIBLE
        binding.btnAccion.setText(
            if (modoRegistro) R.string.accion_crear_cuenta else R.string.accion_iniciar_sesion
        )
        binding.tvCambiarModo.setText(
            if (modoRegistro) R.string.login_ya_tengo_cuenta else R.string.login_no_tengo_cuenta
        )
    }

    private fun enviar() {
        limpiarErrores()

        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.login_error_email_invalido)
            return
        }
        if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.login_error_password_corta)
            return
        }
        if (modoRegistro) {
            val confirmacion = binding.etPasswordConfirm.text?.toString().orEmpty()
            if (password != confirmacion) {
                binding.tilPasswordConfirm.error = getString(R.string.login_error_password_no_coincide)
                return
            }
        }

        mostrarCargando(true)
        if (modoRegistro) registrar(email, password) else iniciarSesion(email, password)
    }

    private fun registrar(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { resultado ->
                if (!resultado.isSuccessful) {
                    mostrarCargando(false)
                    mostrarError(resultado.exception)
                    return@addOnCompleteListener
                }
                // Se marca la cuenta con su rol antes de entrar.
                val cambio = UserProfileChangeRequest.Builder().setDisplayName(rol).build()
                auth.currentUser?.updateProfile(cambio)
                    ?.addOnCompleteListener(this) {
                        mostrarCargando(false)
                        irAPrincipal()
                    } ?: run {
                    mostrarCargando(false)
                    irAPrincipal()
                }
            }
    }

    private fun iniciarSesion(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { resultado ->
                mostrarCargando(false)
                if (!resultado.isSuccessful) {
                    mostrarError(resultado.exception)
                    return@addOnCompleteListener
                }
                val rolCuenta = auth.currentUser?.displayName
                if (rolCuenta != rol) {
                    // La cuenta existe pero es del otro rol: no se permite entrar.
                    auth.signOut()
                    Toast.makeText(
                        this,
                        getString(R.string.login_error_rol_incorrecto, etiquetaRol(rol)),
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnCompleteListener
                }
                irAPrincipal()
            }
    }

    private fun recuperarPassword() {
        limpiarErrores()

        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.login_error_email_invalido)
            return
        }

        mostrarCargando(true)
        auth.sendPasswordResetEmail(email).addOnCompleteListener(this) { resultado ->
            mostrarCargando(false)
            if (resultado.isSuccessful) {
                Toast.makeText(this, R.string.login_reset_enviado, Toast.LENGTH_LONG).show()
            } else {
                mostrarError(resultado.exception)
            }
        }
    }

    private fun mostrarError(excepcion: Exception?) {
        val mensaje = when (excepcion) {
            is FirebaseAuthInvalidCredentialsException -> R.string.login_error_credenciales
            is FirebaseAuthUserCollisionException -> R.string.login_error_email_en_uso
            is FirebaseAuthInvalidUserException -> R.string.login_error_usuario_no_existe
            is FirebaseNetworkException -> R.string.login_error_red
            else -> R.string.login_error_generico
        }
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    private fun limpiarErrores() {
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilPasswordConfirm.error = null
    }

    private fun mostrarCargando(cargando: Boolean) {
        binding.progress.visibility = if (cargando) View.VISIBLE else View.GONE
        binding.btnAccion.isEnabled = !cargando
        binding.tvCambiarModo.isEnabled = !cargando
        binding.tvOlvidePassword.isEnabled = !cargando
    }

    private fun etiquetaRol(rol: String): String =
        if (rol == Sesion.ROL_PERSONAL) getString(R.string.rol_personal)
        else getString(R.string.rol_profesional)

    private fun irAPrincipal() {
        val destino =
            if (rol == Sesion.ROL_PERSONAL) PersonalMainActivity::class.java
            else MainActivity::class.java
        startActivity(Intent(this, destino))
        finish()
    }
}
