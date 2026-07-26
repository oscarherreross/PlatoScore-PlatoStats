package oscar.platoscore.ui.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import oscar.platoscore.R
import oscar.platoscore.databinding.ActivityPersonalTiradaDetailBinding
import oscar.platoscore.databinding.ItemSerieInputBinding
import oscar.platoscore.models.SeriePersonal
import oscar.platoscore.models.TiradaPersonal
import oscar.platoscore.utils.Extras
import oscar.platoscore.utils.Fechas
import oscar.platoscore.viewmodels.TiradaPersonalViewModel
import java.util.Calendar

/** Alta y edición de una tirada del rol personal, con sus series de 25 platos. */
class PersonalTiradaDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalTiradaDetailBinding
    private val viewModel: TiradaPersonalViewModel by viewModels()

    private val calendario = Calendar.getInstance()
    private val filasSeries = mutableListOf<ItemSerieInputBinding>()

    private var tiradaId = 0
    private var editando = false
    private var prefilled = false

    companion object {
        private const val MAX_SERIES = 20
        private const val MAX_PLATOS = SeriePersonal.PLATOS_POR_SERIE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalTiradaDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tiradaId = intent.getIntExtra(Extras.TIRADA_PERSONAL_ID, 0)
        editando = tiradaId != 0

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setTitle(
            if (editando) R.string.personal_form_titulo_editar else R.string.personal_form_titulo_nueva
        )
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.etFechaHora.setOnClickListener { abrirSelectorFechaHora() }
        binding.etNumeroSeries.doAfterTextChanged {
            val n = it?.toString()?.toIntOrNull() ?: 0
            if (n in 1..MAX_SERIES) construirFilas(n)
        }
        binding.btnGuardar.setOnClickListener { guardar() }

        if (editando) {
            cargarExistente()
        } else {
            actualizarCampoFechaHora()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun cargarExistente() {
        viewModel.get(tiradaId).observe(this) { conSeries ->
            if (conSeries == null || prefilled) return@observe
            prefilled = true

            val t = conSeries.tirada
            binding.etLugar.setText(t.lugar)
            binding.etEscuadra.setText(t.numeroEscuadra.toString())
            binding.etNotas.setText(t.notas)
            if (t.tipo == TiradaPersonal.TIPO_COMPETICION) {
                binding.rbCompeticion.isChecked = true
            } else {
                binding.rbEntrenamiento.isChecked = true
            }
            when (t.maquina) {
                TiradaPersonal.MAQUINA_TRAP -> binding.rbTrap.isChecked = true
                TiradaPersonal.MAQUINA_OLIMPICO -> binding.rbOlimpico.isChecked = true
                else -> binding.rbRobot.isChecked = true
            }
            calendario.timeInMillis = t.fechaHora
            actualizarCampoFechaHora()

            val series = conSeries.series.sortedBy { it.numeroSerie }
            // Al fijar el número de series se construyen las filas; luego se rellenan.
            binding.etNumeroSeries.setText(series.size.toString())
            series.forEachIndexed { i, serie ->
                filasSeries.getOrNull(i)?.let { fila ->
                    fila.etPuesto.setText(serie.puesto.toString())
                    fila.etPlatosRotos.setText(serie.platosRotos.toString())
                    fila.etPrimerTiro.setText(serie.platosPrimerTiro?.toString().orEmpty())
                }
            }
        }
    }

    /**
     * Reconstruye las filas de series conservando los valores ya escritos en
     * las posiciones que se mantienen.
     */
    private fun construirFilas(n: Int) {
        val previos = filasSeries.map {
            it.etPlatosRotos.text?.toString().orEmpty() to it.etPrimerTiro.text?.toString().orEmpty()
        }

        binding.containerSeries.removeAllViews()
        filasSeries.clear()

        for (i in 0 until n) {
            val fila = ItemSerieInputBinding.inflate(layoutInflater, binding.containerSeries, false)
            fila.tvSerieLabel.text = getString(R.string.serie_label, i + 1)
            previos.getOrNull(i)?.let { (rotos, primer) ->
                fila.etPlatosRotos.setText(rotos)
                fila.etPrimerTiro.setText(primer)
            }
            binding.containerSeries.addView(fila.root)
            filasSeries.add(fila)
        }
    }

    private fun abrirSelectorFechaHora() {
        DatePickerDialog(this, { _, anio, mes, dia ->
            calendario.set(Calendar.YEAR, anio)
            calendario.set(Calendar.MONTH, mes)
            calendario.set(Calendar.DAY_OF_MONTH, dia)
            TimePickerDialog(this, { _, hora, minuto ->
                calendario.set(Calendar.HOUR_OF_DAY, hora)
                calendario.set(Calendar.MINUTE, minuto)
                actualizarCampoFechaHora()
            }, calendario.get(Calendar.HOUR_OF_DAY), calendario.get(Calendar.MINUTE), true).show()
        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun actualizarCampoFechaHora() {
        binding.etFechaHora.setText(Fechas.mostrarFechaHora(calendario.timeInMillis))
    }

    private fun guardar() {
        val lugar = binding.etLugar.text?.toString()?.trim().orEmpty()
        if (lugar.isBlank()) {
            toast(R.string.personal_error_lugar); return
        }
        val escuadra = binding.etEscuadra.text?.toString()?.toIntOrNull()
        if (escuadra == null || escuadra < 1) {
            toast(R.string.personal_error_escuadra); return
        }
        if (filasSeries.isEmpty()) {
            toast(R.string.personal_error_series); return
        }

        val series = mutableListOf<SeriePersonal>()
        filasSeries.forEachIndexed { i, fila ->
            val puesto = fila.etPuesto.text?.toString()?.toIntOrNull()
            if (puesto == null || puesto < 1) {
                toast(getString(R.string.personal_error_puesto_serie, i + 1)); return
            }
            val rotos = fila.etPlatosRotos.text?.toString()?.toIntOrNull()
            if (rotos == null || rotos !in 0..MAX_PLATOS) {
                toast(getString(R.string.personal_error_platos_serie, i + 1)); return
            }
            val primerTexto = fila.etPrimerTiro.text?.toString()?.trim().orEmpty()
            val primer = if (primerTexto.isEmpty()) null else primerTexto.toIntOrNull()
            if (primerTexto.isNotEmpty() && (primer == null || primer !in 0..MAX_PLATOS || primer > rotos)) {
                toast(getString(R.string.personal_error_primer_tiro, i + 1)); return
            }
            series.add(
                SeriePersonal(
                    numeroSerie = i + 1,
                    puesto = puesto,
                    platosRotos = rotos,
                    platosPrimerTiro = primer
                )
            )
        }

        val tipo =
            if (binding.rbCompeticion.isChecked) TiradaPersonal.TIPO_COMPETICION
            else TiradaPersonal.TIPO_ENTRENAMIENTO

        val maquina = when {
            binding.rbTrap.isChecked -> TiradaPersonal.MAQUINA_TRAP
            binding.rbOlimpico.isChecked -> TiradaPersonal.MAQUINA_OLIMPICO
            else -> TiradaPersonal.MAQUINA_ROBOT
        }

        val tirada = TiradaPersonal(
            id = tiradaId,
            lugar = lugar,
            fechaHora = calendario.timeInMillis,
            numeroEscuadra = escuadra,
            tipo = tipo,
            maquina = maquina,
            notas = binding.etNotas.text?.toString()?.trim().orEmpty()
        )

        if (editando) viewModel.actualizar(tirada, series) else viewModel.guardarNueva(tirada, series)
        Toast.makeText(this, R.string.toast_tirada_personal_guardada, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun toast(resId: Int) = Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    private fun toast(texto: String) = Toast.makeText(this, texto, Toast.LENGTH_SHORT).show()
}
