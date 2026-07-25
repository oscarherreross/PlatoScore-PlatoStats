package oscar.platoscore.ui.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import oscar.platoscore.R
import oscar.platoscore.databinding.ActivityEstadisticasBinding
import oscar.platoscore.models.EstadisticasPersonales
import oscar.platoscore.models.ResumenEstadisticas
import oscar.platoscore.viewmodels.TiradaPersonalViewModel

/** Muestra la evolución del tirador (rol personal) a lo largo de sus tiradas. */
class EstadisticasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEstadisticasBinding
    private val viewModel: TiradaPersonalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEstadisticasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setTitle(R.string.estadisticas_titulo)

        viewModel.todas.observe(this) { tiradas ->
            mostrar(EstadisticasPersonales.calcular(tiradas))
        }
    }

    private fun mostrar(resumen: ResumenEstadisticas) {
        if (!resumen.hayDatos) {
            binding.tvVacio.visibility = View.VISIBLE
            binding.contenido.visibility = View.GONE
            return
        }
        binding.tvVacio.visibility = View.GONE
        binding.contenido.visibility = View.VISIBLE

        binding.tvNumTiradas.text =
            getString(R.string.estadisticas_num_tiradas, resumen.numTiradas)
        binding.tvMediaPlatos.text =
            getString(R.string.estadisticas_media_platos, "%.1f".format(resumen.mediaPlatos))
        binding.tvMediaPorcentaje.text =
            getString(R.string.estadisticas_media_porcentaje, "%.1f".format(resumen.mediaPorcentaje))
        binding.tvMejor.text =
            getString(R.string.estadisticas_mejor, "%.1f".format(resumen.mejorPorcentaje))

        val mediaPrimer = resumen.mediaPorcentajePrimerTiro
        if (mediaPrimer != null) {
            binding.tvMediaPrimerTiro.visibility = View.VISIBLE
            binding.tvMediaPrimerTiro.text =
                getString(R.string.estadisticas_media_primer_tiro, "%.1f".format(mediaPrimer))
        } else {
            binding.tvMediaPrimerTiro.visibility = View.GONE
        }

        binding.chart.setValores(resumen.puntos.map { it.porcentaje })
    }
}
