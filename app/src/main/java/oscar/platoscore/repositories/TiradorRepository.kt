package oscar.platoscore.repositories

import androidx.lifecycle.LiveData
import oscar.platoscore.database.TiradorDao
import oscar.platoscore.models.Tirador

class TiradorRepository(private val tiradorDao: TiradorDao) {

    fun getTiradores(escuadraId: Int): LiveData<List<Tirador>> =
        tiradorDao.getTiradores(escuadraId)

    fun getTiradoresByTirada(tiradaId: Int): LiveData<List<Tirador>> =
        tiradorDao.getTiradoresByTirada(tiradaId)

    suspend fun insert(tirador: Tirador) = tiradorDao.insert(tirador)

    suspend fun update(tirador: Tirador) = tiradorDao.update(tirador)

    suspend fun delete(tirador: Tirador) = tiradorDao.delete(tirador)
}
