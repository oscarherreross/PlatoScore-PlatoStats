package oscar.platoscore.repositories

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import oscar.platoscore.database.TiradaDao
import oscar.platoscore.models.Tirada

class TiradaRepository(private val tiradaDao: TiradaDao) {

    val allTiradas: LiveData<List<Tirada>> = tiradaDao.getAllTiradas()

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

    suspend fun deleteTiradaById(id: Int) = withContext(Dispatchers.IO) {
        tiradaDao.deleteTiradaById(id)
    }
}
