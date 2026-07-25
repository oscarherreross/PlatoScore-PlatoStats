package oscar.platoscore.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import oscar.platoscore.database.PlatoScoreDatabase
import oscar.platoscore.models.SeriePersonal
import oscar.platoscore.models.TiradaPersonal
import oscar.platoscore.models.TiradaPersonalConSeries
import oscar.platoscore.repositories.TiradaPersonalRepository
import oscar.platoscore.utils.Sesion

class TiradaPersonalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TiradaPersonalRepository
    private val uid = Sesion.uid()

    val todas: LiveData<List<TiradaPersonalConSeries>>

    init {
        val dao = PlatoScoreDatabase.getDatabase(application).tiradaPersonalDao()
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
