package oscar.platoscore.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import oscar.platoscore.databinding.ActivityRoleSelectionBinding
import oscar.platoscore.utils.Extras
import oscar.platoscore.utils.Sesion

/**
 * Primera pantalla de la app: elegir entre entrar como profesional o como
 * personal. Cada rol lleva a su propio inicio de sesión (con credenciales
 * independientes). Si ya hay una sesión abierta del rol pulsado, entra directo.
 */
class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnProfesional.setOnClickListener { entrarComo(Sesion.ROL_PROFESIONAL) }
        binding.btnPersonal.setOnClickListener { entrarComo(Sesion.ROL_PERSONAL) }
    }

    private fun entrarComo(rol: String) {
        val usuario = FirebaseAuth.getInstance().currentUser
        if (usuario != null && usuario.displayName == rol) {
            // Ya hay sesión de ese rol: se entra directamente a su pantalla.
            startActivity(Intent(this, homeDe(rol)))
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra(Extras.ROL, rol)
            startActivity(intent)
        }
    }

    private fun homeDe(rol: String): Class<*> =
        if (rol == Sesion.ROL_PERSONAL) PersonalMainActivity::class.java
        else MainActivity::class.java
}
