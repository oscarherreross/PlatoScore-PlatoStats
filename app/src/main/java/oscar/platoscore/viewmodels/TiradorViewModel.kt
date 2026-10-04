package oscar.platoscore.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import oscar.plato.core.viewmodels.GuardadoViewModel
import oscar.platoscore.database.PlatoScoreDatabase
import oscar.platoscore.models.Tirador
import oscar.platoscore.repositories.TiradorRepository

class TiradorViewModel(application: Application) : GuardadoViewModel(application) {

    private val tiradorRepository: TiradorRepository

    init {
        val tiradorDao = PlatoScoreDatabase.getDatabase(application).tiradorDao()
        tiradorRepository = TiradorRepository(tiradorDao)
    }

    fun getTiradores(escuadraId: Int): LiveData<List<Tirador>> =
        tiradorRepository.getTiradores(escuadraId)

    fun getAllTiradores(): LiveData<List<Tirador>> =
        tiradorRepository.getAllTiradores()

    fun getTiradoresByTirada(tiradaId: Int): LiveData<List<Tirador>> =
        tiradorRepository.getTiradoresByTirada(tiradaId)

    fun insertTirador(tirador: Tirador) {
        guardar {
            tiradorRepository.insert(tirador)
        }
    }

    fun updateTirador(tirador: Tirador) {
        guardar {
            tiradorRepository.update(tirador)
        }
    }

    fun deleteTirador(tirador: Tirador) {
        guardar {
            tiradorRepository.delete(tirador)
        }
    }
}
