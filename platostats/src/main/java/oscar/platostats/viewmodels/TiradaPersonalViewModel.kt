package oscar.platostats.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import oscar.plato.core.utils.Sesion
import oscar.platostats.database.PlatoStatsDatabase
import oscar.platostats.models.SeriePersonal
import oscar.platostats.models.TiradaPersonal
import oscar.platostats.models.TiradaPersonalConSeries
import oscar.platostats.repositories.TiradaPersonalRepository

class TiradaPersonalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TiradaPersonalRepository
    private val uid = Sesion.uid()

    val todas: LiveData<List<TiradaPersonalConSeries>>

    init {
        val dao = PlatoStatsDatabase.getDatabase(application).tiradaPersonalDao()
        repository = TiradaPersonalRepository(dao)
        todas = repository.getAll(uid)
    }

    fun get(id: Int): LiveData<TiradaPersonalConSeries?> = repository.get(id)

    fun guardarNueva(tirada: TiradaPersonal, series: List<SeriePersonal>) {
        viewModelScope.launch {
            repository.guardarNueva(tirada.copy(userId = uid), series)
        }
    }

    fun actualizar(tirada: TiradaPersonal, series: List<SeriePersonal>) {
        viewModelScope.launch {
            repository.actualizar(tirada.copy(userId = uid), series)
        }
    }

    fun eliminar(tirada: TiradaPersonal) {
        viewModelScope.launch {
            repository.eliminar(tirada)
        }
    }
}
