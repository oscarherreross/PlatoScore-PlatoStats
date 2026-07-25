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
 * personal. Es solo el modo de uso; una misma cuenta sirve para ambos. Si ya
 * hay una sesión abierta, se entra directo al modo pulsado; si no, se pasa por
 * el inicio de sesión.
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
        if (FirebaseAuth.getInstance().currentUser != null) {
            // Ya hay sesión: se entra directo al modo elegido.
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
