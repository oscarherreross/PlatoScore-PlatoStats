package oscar.platostats.ui.activities

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import androidx.core.widget.doAfterTextChanged
import kotlinx.parcelize.Parcelize
import oscar.plato.core.ui.DialogosRestaurables
import oscar.plato.core.utils.Fechas
import oscar.plato.core.utils.InsetsUtil
import oscar.plato.core.utils.enableEdgeToEdgeConToolbar
import oscar.platostats.R
import oscar.platostats.databinding.ActivityTiradaDetailBinding
import oscar.platostats.databinding.ItemSerieInputBinding
import oscar.platostats.models.Serie
import oscar.platostats.models.Tirada
import oscar.platostats.utils.Extras
import oscar.platostats.viewmodels.TiradaViewModel
import java.util.Calendar

/** Lo escrito en la fila de una serie, tal cual (puede estar a medias o vacío). */
@Parcelize
private data class TextosSerie(
    val puesto: String,
    val platosRotos: String,
    val primerTiro: String
) : Parcelable

/** Alta y edición de una tirada, con sus series de 25 platos. */
class TiradaDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTiradaDetailBinding
    private val viewModel: TiradaViewModel by viewModels()

    private val dialogos = DialogosRestaurables(this)

    private val calendario = Calendar.getInstance()
    private val filasSeries = mutableListOf<ItemSerieInputBinding>()

    private var tiradaId = 0
    private var editando = false

    /**
     * El formulario ya tiene sus datos: en un alta, desde el principio; en una
     * edición, desde que se cargan de la base de datos. A partir de ahí manda lo
     * que haya en pantalla, también cuando esta se recrea (giro).
     */
    private var formularioListo = false

    companion object {
        private const val MAX_SERIES = 20
        private const val MAX_PLATOS = Serie.PLATOS_POR_SERIE

        private const val DIALOGO_FECHA = "fecha"
        private const val DIALOGO_HORA = "hora"

        private const val ESTADO_FORMULARIO_LISTO = "formulario_listo"
        private const val ESTADO_FECHA_HORA = "fecha_hora"
        private const val ESTADO_SERIES = "series"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeConToolbar()
        binding = ActivityTiradaDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        InsetsUtil.padTop(binding.toolbar)
        InsetsUtil.addMarginBottom(binding.btnGuardar)

        tiradaId = intent.getIntExtra(Extras.TIRADA_ID, 0)
        editando = tiradaId != 0

        formularioListo = savedInstanceState?.getBoolean(ESTADO_FORMULARIO_LISTO) ?: !editando
        savedInstanceState?.let { calendario.timeInMillis = it.getLong(ESTADO_FECHA_HORA) }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setTitle(
            if (editando) R.string.form_titulo_editar else R.string.form_titulo_nueva
        )
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Registrados aquí, los selectores siguen abiertos si la pantalla se recrea (giro).
        dialogos.registrar(DIALOGO_FECHA) { crearSelectorFecha() }
        dialogos.registrar(DIALOGO_HORA) { crearSelectorHora() }

        binding.etFechaHora.setOnClickListener { dialogos.mostrar(DIALOGO_FECHA) }
        binding.etNumeroSeries.doAfterTextChanged {
            val n = it?.toString()?.toIntOrNull() ?: 0
            if (n in 1..MAX_SERIES) construirFilas(n)
        }
        binding.btnGuardar.setOnClickListener { guardar() }

        if (formularioListo) {
            actualizarCampoFechaHora()
        } else {
            cargarExistente()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ESTADO_FORMULARIO_LISTO, formularioListo)
        outState.putLong(ESTADO_FECHA_HORA, calendario.timeInMillis)
        // Las filas de series se crean por código y comparten id: Android no sabe
        // guardar lo escrito en ellas, así que se guarda aquí.
        outState.putParcelableArrayList(ESTADO_SERIES, ArrayList(textosDeFilas()))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        // Al reponerse el nº de series se vuelven a crear las filas, vacías.
        super.onRestoreInstanceState(savedInstanceState)
        val textos = BundleCompat
            .getParcelableArrayList(savedInstanceState, ESTADO_SERIES, TextosSerie::class.java)
            .orEmpty()
        if (filasSeries.size != textos.size) construirFilas(textos.size)
        rellenarFilas(textos)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun cargarExistente() {
        viewModel.get(tiradaId).observe(this) { conSeries ->
            if (conSeries == null || formularioListo) return@observe
            formularioListo = true

            val t = conSeries.tirada
            binding.etLugar.setText(t.lugar)
            binding.etEscuadra.setText(t.numeroEscuadra.toString())
            binding.etNotas.setText(t.notas)
            if (t.tipo == Tirada.TIPO_COMPETICION) {
                binding.rbCompeticion.isChecked = true
            } else {
                binding.rbEntrenamiento.isChecked = true
            }
            when (t.maquina) {
                Tirada.MAQUINA_TRAP -> binding.rbTrap.isChecked = true
                Tirada.MAQUINA_OLIMPICO -> binding.rbOlimpico.isChecked = true
                else -> binding.rbRobot.isChecked = true
            }
            calendario.timeInMillis = t.fechaHora
            actualizarCampoFechaHora()

            val series = conSeries.series.sortedBy { it.numeroSerie }
            // Al fijar el número de series se construyen las filas; luego se rellenan.
            binding.etNumeroSeries.setText(series.size.toString())
            rellenarFilas(series.map {
                TextosSerie(
                    puesto = it.puesto.toString(),
                    platosRotos = it.platosRotos.toString(),
                    primerTiro = it.platosPrimerTiro?.toString().orEmpty()
                )
            })
        }
    }

    /**
     * Reconstruye las filas de series conservando los valores ya escritos en
     * las posiciones que se mantienen.
     */
    private fun construirFilas(n: Int) {
        val previos = textosDeFilas()

        binding.containerSeries.removeAllViews()
        filasSeries.clear()

        for (i in 0 until n) {
            val fila = ItemSerieInputBinding.inflate(layoutInflater, binding.containerSeries, false)
            fila.tvSerieLabel.text = getString(R.string.serie_label, i + 1)
            binding.containerSeries.addView(fila.root)
            filasSeries.add(fila)
        }
        rellenarFilas(previos)
    }

    private fun textosDeFilas(): List<TextosSerie> = filasSeries.map {
        TextosSerie(
            puesto = it.etPuesto.text?.toString().orEmpty(),
            platosRotos = it.etPlatosRotos.text?.toString().orEmpty(),
            primerTiro = it.etPrimerTiro.text?.toString().orEmpty()
        )
    }

    /** Escribe [textos] en las filas, por orden; las filas o los textos que sobren se ignoran. */
    private fun rellenarFilas(textos: List<TextosSerie>) {
        filasSeries.zip(textos).forEach { (fila, texto) ->
            fila.etPuesto.setText(texto.puesto)
            fila.etPlatosRotos.setText(texto.platosRotos)
            fila.etPrimerTiro.setText(texto.primerTiro)
        }
    }

    /** Primero se elige el día y, al aceptarlo, la hora. */
    private fun crearSelectorFecha(): Dialog =
        DatePickerDialog(this, { _, anio, mes, dia ->
            calendario.set(Calendar.YEAR, anio)
            calendario.set(Calendar.MONTH, mes)
            calendario.set(Calendar.DAY_OF_MONTH, dia)
            actualizarCampoFechaHora()
            dialogos.mostrar(DIALOGO_HORA)
        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH))

    private fun crearSelectorHora(): Dialog =
        TimePickerDialog(this, { _, hora, minuto ->
            calendario.set(Calendar.HOUR_OF_DAY, hora)
            calendario.set(Calendar.MINUTE, minuto)
            actualizarCampoFechaHora()
        }, calendario.get(Calendar.HOUR_OF_DAY), calendario.get(Calendar.MINUTE), true)

    private fun actualizarCampoFechaHora() {
        binding.etFechaHora.setText(Fechas.mostrarFechaHora(calendario.timeInMillis))
    }

    private fun guardar() {
        val lugar = binding.etLugar.text?.toString()?.trim().orEmpty()
        if (lugar.isBlank()) {
            toast(R.string.error_lugar); return
        }
        val escuadra = binding.etEscuadra.text?.toString()?.toIntOrNull()
        if (escuadra == null || escuadra < 1) {
            toast(R.string.error_escuadra); return
        }
        if (filasSeries.isEmpty()) {
            toast(R.string.error_series); return
        }

        val series = mutableListOf<Serie>()
        filasSeries.forEachIndexed { i, fila ->
            val puesto = fila.etPuesto.text?.toString()?.toIntOrNull()
            if (puesto == null || puesto < 1) {
                toast(getString(R.string.error_puesto_serie, i + 1)); return
            }
            val rotos = fila.etPlatosRotos.text?.toString()?.toIntOrNull()
            if (rotos == null || rotos !in 0..MAX_PLATOS) {
                toast(getString(R.string.error_platos_serie, i + 1)); return
            }
            val primerTexto = fila.etPrimerTiro.text?.toString()?.trim().orEmpty()
            val primer = if (primerTexto.isEmpty()) null else primerTexto.toIntOrNull()
            if (primerTexto.isNotEmpty() && (primer == null || primer !in 0..MAX_PLATOS || primer > rotos)) {
                toast(getString(R.string.error_primer_tiro, i + 1)); return
            }
            series.add(
                Serie(
                    numeroSerie = i + 1,
                    puesto = puesto,
                    platosRotos = rotos,
                    platosPrimerTiro = primer
                )
            )
        }

        val tipo =
            if (binding.rbCompeticion.isChecked) Tirada.TIPO_COMPETICION
            else Tirada.TIPO_ENTRENAMIENTO

        val maquina = when {
            binding.rbTrap.isChecked -> Tirada.MAQUINA_TRAP
            binding.rbOlimpico.isChecked -> Tirada.MAQUINA_OLIMPICO
            else -> Tirada.MAQUINA_ROBOT
        }

        val tirada = Tirada(
            id = tiradaId,
            lugar = lugar,
            fechaHora = calendario.timeInMillis,
            numeroEscuadra = escuadra,
            tipo = tipo,
            maquina = maquina,
            notas = binding.etNotas.text?.toString()?.trim().orEmpty()
        )

        if (editando) viewModel.actualizar(tirada, series) else viewModel.guardarNueva(tirada, series)
        Toast.makeText(this, R.string.toast_tirada_guardada, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun toast(resId: Int) = Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    private fun toast(texto: String) = Toast.makeText(this, texto, Toast.LENGTH_SHORT).show()
}
