package oscar.platostats.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import oscar.plato.core.utils.Sesion
import oscar.platostats.database.PlatoStatsDatabase
import oscar.platostats.models.Serie
import oscar.platostats.models.Tirada
import oscar.platostats.models.TiradaConSeries
import oscar.platostats.repositories.TiradaRepository

class TiradaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TiradaRepository
    private val uid = Sesion.uid()

    val todas: LiveData<List<TiradaConSeries>>

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
}
