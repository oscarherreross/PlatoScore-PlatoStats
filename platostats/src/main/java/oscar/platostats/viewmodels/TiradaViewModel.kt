package oscar.platostats.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import oscar.plato.core.models.FiltroTiradas
import oscar.plato.core.utils.Sesion
import oscar.plato.core.viewmodels.GuardadoViewModel
import oscar.platostats.database.PlatoStatsDatabase
import oscar.platostats.models.Serie
import oscar.platostats.models.Tirada
import oscar.platostats.models.TiradaConSeries
import oscar.platostats.repositories.TiradaRepository

class TiradaViewModel(
    application: Application,
    estado: SavedStateHandle
) : GuardadoViewModel(application) {

    private val repository: TiradaRepository
    private val uid = Sesion.uid()

    val todas: LiveData<List<TiradaConSeries>>

    /**
     * Filtro de la pantalla (lista de tiradas o estadísticas). Vive en el
     * SavedStateHandle para que no se pierda cuando Android recrea la pantalla
     * (giro) o cierra el proceso.
     */
    val filtro: MutableLiveData<FiltroTiradas> = estado.getLiveData(CLAVE_FILTRO, FiltroTiradas())

    init {
        val dao = PlatoStatsDatabase.getDatabase(application).tiradaDao()
        repository = TiradaRepository(dao)
        todas = repository.getAll(uid)
    }

    fun get(id: Int): LiveData<TiradaConSeries?> = repository.get(id)

    private val _formularioGuardado = MutableLiveData<Boolean?>(null)

    /**
     * Resultado de guardar el formulario de tirada: true o false al terminar y null
     * mientras no hay nada que contar. El formulario espera a saberlo para cerrarse.
     */
    val formularioGuardado: LiveData<Boolean?> = _formularioGuardado

    private var guardandoFormulario = false

    fun guardarNueva(tirada: Tirada, series: List<Serie>) {
        guardarFormulario { repository.guardarNueva(tirada.copy(userId = uid), series) }
    }

    fun actualizar(tirada: Tirada, series: List<Serie>) {
        guardarFormulario { repository.actualizar(tirada.copy(userId = uid), series) }
    }

    /** El formulario ya ha reaccionado al resultado del guardado. */
    fun formularioGuardadoAtendido() {
        _formularioGuardado.value = null
    }

    private fun guardarFormulario(operacion: suspend () -> Unit) {
        // Una pulsación repetida no debe guardar la tirada dos veces.
        if (guardandoFormulario) return
        guardandoFormulario = true
        guardar(
            alTerminar = { hecho ->
                guardandoFormulario = false
                _formularioGuardado.value = hecho
            },
            operacion = operacion
        )
    }

    fun eliminar(tirada: Tirada) {
        guardar {
            repository.eliminar(tirada)
        }
    }

    private companion object {
        const val CLAVE_FILTRO = "filtro"
    }
}
