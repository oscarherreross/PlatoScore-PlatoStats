package oscar.platostats.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import oscar.plato.core.models.FiltroTiradas
import oscar.plato.core.utils.Sesion
import oscar.platostats.database.PlatoStatsDatabase
import oscar.platostats.models.Serie
import oscar.platostats.models.Tirada
import oscar.platostats.models.TiradaConSeries
import oscar.platostats.repositories.TiradaRepository

class TiradaViewModel(
    application: Application,
    estado: SavedStateHandle
) : AndroidViewModel(application) {

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

    fun guardarNueva(tirada: Tirada, series: List<Serie>) {
        viewModelScope.launch {
            repository.guardarNueva(tirada.copy(userId = uid), series)
        }
    }

    fun actualizar(tirada: Tirada, series: List<Serie>) {
        viewModelScope.launch {
            repository.actualizar(tirada.copy(userId = uid), series)
        }
    }

    fun eliminar(tirada: Tirada) {
        viewModelScope.launch {
            repository.eliminar(tirada)
        }
    }

    private companion object {
        const val CLAVE_FILTRO = "filtro"
    }
}
