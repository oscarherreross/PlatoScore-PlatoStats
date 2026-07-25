package oscar.platoscore.repositories

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import oscar.platoscore.database.TiradaDao
import oscar.platoscore.models.Tirada
import oscar.platoscore.models.TiradaConContadores
import oscar.platoscore.models.TiradorConTirada

class TiradaRepository(private val tiradaDao: TiradaDao) {

    fun allTiradas(userId: String): LiveData<List<TiradaConContadores>> =
        tiradaDao.getAllTiradasConContadores(userId)

    fun tiradoresConTirada(userId: String): LiveData<List<TiradorConTirada>> =
        tiradaDao.getTiradoresConTirada(userId)

    fun getTirada(id: Int): LiveData<Tirada?> = tiradaDao.getTirada(id)

    suspend fun insert(tirada: Tirada): Long = withContext(Dispatchers.IO) {
        tiradaDao.insert(tirada)
    }

    suspend fun update(tirada: Tirada) = withContext(Dispatchers.IO) {
        tiradaDao.update(tirada)
    }

    suspend fun delete(tirada: Tirada) = withContext(Dispatchers.IO) {
        tiradaDao.delete(tirada)
    }
}
