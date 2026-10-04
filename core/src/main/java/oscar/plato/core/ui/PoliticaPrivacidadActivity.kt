package oscar.plato.core.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import oscar.plato.core.R
import oscar.plato.core.databinding.ActivityPoliticaPrivacidadBinding
import oscar.plato.core.models.DatosLegales
import oscar.plato.core.platoApp
import oscar.plato.core.utils.InsetsUtil
import oscar.plato.core.utils.enableEdgeToEdgeConToolbar

/**
 * Política de privacidad de la app en curso. Las páginas de la carpeta legal/ del
 * proyecto van dentro de la app, así que se muestra siempre: sin conexión y aunque
 * todavía no esté publicada en la web. Desde ella se llega a la página de
 * eliminación de cuenta; los demás enlaces se abren fuera de la app.
 */
class PoliticaPrivacidadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPoliticaPrivacidadBinding
    private lateinit var datos: DatosLegales

    private var pagina = PAGINA_PRIVACIDAD

    /** Desde la página de eliminación de cuenta, «atrás» vuelve a la política. */
    private val volverALaPolitica = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = mostrar(PAGINA_PRIVACIDAD)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeConToolbar()
        binding = ActivityPoliticaPrivacidadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        InsetsUtil.padTop(binding.toolbar)
        InsetsUtil.addMarginBottom(binding.webView)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setTitle(R.string.accion_privacidad)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        onBackPressedDispatcher.addCallback(this, volverALaPolitica)

        datos = assets.open(ARCHIVO_DATOS).reader(Charsets.UTF_8).use { DatosLegales.leer(it) }

        prepararModoNoche()
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                abrirEnlace(request.url)
                return true
            }
        }
        mostrar(savedInstanceState?.getString(ESTADO_PAGINA) ?: PAGINA_PRIVACIDAD)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ESTADO_PAGINA, pagina)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    /** Carga una página de legal/ completándola con los datos legales. */
    private fun mostrar(nombre: String) {
        pagina = nombre
        volverALaPolitica.isEnabled = nombre != PAGINA_PRIVACIDAD

        val carpeta = platoApp.carpetaLegal
        val html = assets.open("$carpeta/$nombre").reader(Charsets.UTF_8).use { it.readText() }
        binding.webView.loadDataWithBaseURL(
            "$RAIZ$carpeta/",
            datos.rellenar(html, getString(R.string.legal_dato_pendiente)),
            "text/html",
            "UTF-8",
            null
        )
    }

    private fun abrirEnlace(enlace: Uri) {
        // Enlaces entre las páginas de la app: se cargan aquí, ya completadas.
        if (enlace.scheme == "file") {
            enlace.lastPathSegment?.takeIf { it in PAGINAS }?.let { mostrar(it) }
            return
        }
        try {
            startActivity(Intent(Intent.ACTION_VIEW, enlace))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.toast_enlace_no_disponible, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Las páginas traen su propio tema oscuro. Desde Android 13 el navegador
     * integrado lo aplica según el tema de la app; de Android 10 a 12 hay que pedirlo.
     */
    private fun prepararModoNoche() {
        val noche = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
        if (noche && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            @Suppress("DEPRECATION")
            binding.webView.settings.forceDark = WebSettings.FORCE_DARK_ON
        }
    }

    private companion object {
        const val RAIZ = "file:///android_asset/"
        const val ARCHIVO_DATOS = "datos-legales.properties"
        const val PAGINA_PRIVACIDAD = "privacidad.html"
        const val PAGINA_ELIMINAR_CUENTA = "eliminar-cuenta.html"
        val PAGINAS = setOf(PAGINA_PRIVACIDAD, PAGINA_ELIMINAR_CUENTA)
        const val ESTADO_PAGINA = "pagina"
    }
}
