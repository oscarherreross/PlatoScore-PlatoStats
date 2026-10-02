package oscar.plato.core.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import oscar.plato.core.R
import oscar.plato.core.databinding.ActivitySplashBinding
import oscar.plato.core.nombreApp
import oscar.plato.core.platoApp
import oscar.plato.core.utils.InsetsUtil
import oscar.plato.core.utils.Sesion
import oscar.plato.core.utils.enableEdgeToEdgeConToolbar

/**
 * Pantalla de carga (splash), común a las dos apps. Muestra el logo, el nombre y
 * el lema de la app en curso ([platoApp]) un instante y luego entra en su
 * pantalla principal si ya hay sesión, o en el inicio de sesión si no. El fondo
 * naranja se pinta ya desde windowBackground (Theme.Plato.Splash), así que no
 * hay parpadeo en blanco durante el arranque en frío.
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())

    private val continuar = Runnable {
        if (isFinishing || isDestroyed) return@Runnable
        val destino = if (Sesion.iniciada()) platoApp.pantallaPrincipal else LoginActivity::class.java
        startActivity(Intent(this, destino))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeConToolbar()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        InsetsUtil.addMarginBottom(binding.progress)

        binding.ivLogo.setImageResource(platoApp.logo)
        binding.ivLogo.contentDescription = getString(R.string.cd_logo, nombreApp)
        binding.tvNombre.text = nombreApp
        binding.tvTagline.setText(platoApp.lema)

        animarEntrada()
        handler.postDelayed(continuar, DURACION_MS)
    }

    /** El logo aparece con un ligero fundido y rebote. */
    private fun animarEntrada() {
        binding.grupoLogo.apply {
            alpha = 0f
            scaleX = 0.85f
            scaleY = 0.85f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(520L)
                .setInterpolator(OvershootInterpolator(1.6f))
                .start()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(continuar)
        super.onDestroy()
    }

    companion object {
        private const val DURACION_MS = 1350L
    }
}
