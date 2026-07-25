package oscar.platoscore.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import oscar.platoscore.database.PlatoScoreDatabase
import oscar.platoscore.models.Tirada
import oscar.platoscore.models.TiradaConContadores
import oscar.platoscore.repositories.TiradaRepository
import oscar.platoscore.utils.Sesion

class TiradaViewModel(application: Application) : AndroidViewModel(application) {

    private val tiradaRepository: TiradaRepository
    private val uid = Sesion.uid()

    val allTiradas: LiveData<List<TiradaConContadores>>

    init {
        val tiradaDao = PlatoScoreDatabase.getDatabase(application).tiradaDao()
        tiradaRepository = TiradaRepository(tiradaDao)
        allTiradas = tiradaRepository.allTiradas(uid)
    }

    fun getTirada(id: Int): LiveData<Tirada?> = tiradaRepository.getTirada(id)

    fun insertTirada(tirada: Tirada) {
        viewModelScope.launch {
            // Se sella con el UID del profesional autenticado para aislar sus datos.
            tiradaRepository.insert(tirada.copy(userId = uid))
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
