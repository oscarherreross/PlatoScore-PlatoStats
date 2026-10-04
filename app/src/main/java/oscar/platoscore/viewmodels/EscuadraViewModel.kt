package oscar.platoscore.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import oscar.plato.core.viewmodels.GuardadoViewModel
import oscar.platoscore.database.PlatoScoreDatabase
import oscar.platoscore.models.Escuadra
import oscar.platoscore.models.EscuadraConContadores
import oscar.platoscore.repositories.EscuadraRepository

class EscuadraViewModel(application: Application) : GuardadoViewModel(application) {

    private val escuadraRepository: EscuadraRepository

    init {
        val escuadraDao = PlatoScoreDatabase.getDatabase(application).escuadraDao()
        escuadraRepository = EscuadraRepository(escuadraDao)
    }

    fun getEscuadrasByTirada(tiradaId: Int): LiveData<List<EscuadraConContadores>> =
        escuadraRepository.getEscuadrasByTirada(tiradaId)

    /**
     * Crea una escuadra nueva numerada con MAX(numeroEscuadra) + 1, para que
     * no se repitan números aunque se hayan borrado escuadras intermedias.
     */
    fun crearEscuadra(tiradaId: Int) {
        guardar {
            val numero = escuadraRepository.getSiguienteNumeroEscuadra(tiradaId)
            escuadraRepository.insert(Escuadra(tiradaId = tiradaId, numeroEscuadra = numero))
        }
    }

    fun deleteEscuadra(escuadra: Escuadra) {
        guardar {
            escuadraRepository.delete(escuadra)
        }
    }
}
