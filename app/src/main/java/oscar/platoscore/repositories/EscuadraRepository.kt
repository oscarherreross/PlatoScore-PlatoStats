package oscar.platoscore.repositories

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import oscar.platoscore.database.EscuadraDao
import oscar.platoscore.models.Escuadra

class EscuadraRepository(private val escuadraDao: EscuadraDao) {

    fun getEscuadrasByTirada(tiradaId: Int): LiveData<List<Escuadra>> =
        escuadraDao.getEscuadrasByTirada(tiradaId)

    fun getEscuadra(id: Int): LiveData<Escuadra> = escuadraDao.getEscuadra(id)

    suspend fun insert(escuadra: Escuadra): Long = withContext(Dispatchers.IO) {
        escuadraDao.insert(escuadra)
    }

    suspend fun update(escuadra: Escuadra) = withContext(Dispatchers.IO) {
        escuadraDao.update(escuadra)
    }

    suspend fun delete(escuadra: Escuadra) = withContext(Dispatchers.IO) {
        escuadraDao.delete(escuadra)
    }
}
