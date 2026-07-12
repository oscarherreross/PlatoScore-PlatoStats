package oscar.platoscore.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import oscar.platoscore.database.PlatoScoreDatabase
import oscar.platoscore.models.Escuadra
import oscar.platoscore.repositories.EscuadraRepository

class EscuadraViewModel(application: Application) : AndroidViewModel(application) {

    private val escuadraRepository: EscuadraRepository

    init {
        val escuadraDao = PlatoScoreDatabase.getDatabase(application).escuadraDao()
        escuadraRepository = EscuadraRepository(escuadraDao)
    }

    fun getEscuadrasByTirada(tiradaId: Int): LiveData<List<Escuadra>> =
        escuadraRepository.getEscuadrasByTirada(tiradaId)

    fun getEscuadra(id: Int): LiveData<Escuadra> = escuadraRepository.getEscuadra(id)

    fun insertEscuadra(escuadra: Escuadra) {
        viewModelScope.launch {
            escuadraRepository.insert(escuadra)
        }
    }

    fun updateEscuadra(escuadra: Escuadra) {
        viewModelScope.launch {
            escuadraRepository.update(escuadra)
        }
    }

    fun deleteEscuadra(escuadra: Escuadra) {
        viewModelScope.launch {
            escuadraRepository.delete(escuadra)
        }
    }
}
