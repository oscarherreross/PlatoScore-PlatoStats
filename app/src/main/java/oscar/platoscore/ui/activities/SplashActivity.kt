package oscar.platoscore.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import oscar.platoscore.databinding.ActivitySplashBinding
import oscar.platoscore.utils.InsetsUtil
import oscar.platoscore.utils.enableEdgeToEdgeConToolbar

/**
 * Pantalla de carga (splash). Muestra el logo y el nombre de la app un instante
 * y luego pasa a [RoleSelectionActivity]. El fondo naranja se pinta ya desde
 * windowBackground (Theme.PlatoScore.Splash), así que no hay parpadeo en blanco
 * durante el arranque en frío.
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())

    private val irAlInicio = Runnable {
        if (isFinishing || isDestroyed) return@Runnable
        startActivity(Intent(this, RoleSelectionActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeConToolbar()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        InsetsUtil.addMarginBottom(binding.progress)

        animarEntrada()
        handler.postDelayed(irAlInicio, DURACION_MS)
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
        handler.removeCallbacks(irAlInicio)
        super.onDestroy()
    }

    companion object {
        private const val DURACION_MS = 1350L
    }
}
