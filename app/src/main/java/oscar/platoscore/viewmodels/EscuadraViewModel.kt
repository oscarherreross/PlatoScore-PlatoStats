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

    /**
     * Crea una escuadra nueva numerada con MAX(numeroEscuadra) + 1, para que
     * no se repitan números aunque se hayan borrado escuadras intermedias.
     */
    fun crearEscuadra(tiradaId: Int) {
        viewModelScope.launch {
            val numero = escuadraRepository.getSiguienteNumeroEscuadra(tiradaId)
            escuadraRepository.insert(Escuadra(tiradaId = tiradaId, numeroEscuadra = numero))
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
