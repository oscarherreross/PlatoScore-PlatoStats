package oscar.platoscore.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import oscar.platoscore.database.PlatoScoreDatabase
import oscar.platoscore.models.Tirada
import oscar.platoscore.repositories.TiradaRepository

class TiradaViewModel(application: Application) : AndroidViewModel(application) {

    private val tiradaRepository: TiradaRepository

    val allTiradas: LiveData<List<Tirada>>

    init {
        val tiradaDao = PlatoScoreDatabase.getDatabase(application).tiradaDao()
        tiradaRepository = TiradaRepository(tiradaDao)
        allTiradas = tiradaRepository.allTiradas
    }

    fun getTirada(id: Int): LiveData<Tirada?> = tiradaRepository.getTirada(id)

    fun insertTirada(tirada: Tirada) {
        viewModelScope.launch {
            tiradaRepository.insert(tirada)
        }
    }

    fun updateTirada(tirada: Tirada) {
        viewModelScope.launch {
            tiradaRepository.update(tirada)
        }
    }

    fun deleteTirada(tirada: Tirada) {
        viewModelScope.launch {
            tiradaRepository.delete(tirada)
        }
    }
}
