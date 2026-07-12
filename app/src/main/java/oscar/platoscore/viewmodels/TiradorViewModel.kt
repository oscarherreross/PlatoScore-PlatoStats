package oscar.platoscore.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import oscar.platoscore.database.PlatoScoreDatabase
import oscar.platoscore.models.Tirador
import oscar.platoscore.repositories.TiradorRepository

class TiradorViewModel(application: Application) : AndroidViewModel(application) {

    private val tiradorRepository: TiradorRepository

    init {
        val tiradorDao = PlatoScoreDatabase.getDatabase(application).tiradorDao()
        tiradorRepository = TiradorRepository(tiradorDao)
    }

    fun getTiradores(escuadraId: Int): LiveData<List<Tirador>> =
        tiradorRepository.getTiradores(escuadraId)

    fun getTiradoresByTirada(tiradaId: Int): LiveData<List<Tirador>> =
        tiradorRepository.getTiradoresByTirada(tiradaId)

    fun insertTirador(tirador: Tirador) {
        viewModelScope.launch {
            tiradorRepository.insert(tirador)
        }
    }

    fun updateTirador(tirador: Tirador) {
        viewModelScope.launch {
            tiradorRepository.update(tirador)
        }
    }

    fun deleteTirador(tirador: Tirador) {
        viewModelScope.launch {
            tiradorRepository.delete(tirador)
        }
    }
}
