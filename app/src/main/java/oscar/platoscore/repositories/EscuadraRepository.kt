package oscar.platoscore.repositories

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import oscar.platoscore.database.EscuadraDao
import oscar.platoscore.models.Escuadra
import oscar.platoscore.models.EscuadraConContadores

class EscuadraRepository(private val escuadraDao: EscuadraDao) {

    fun getEscuadrasByTirada(tiradaId: Int): LiveData<List<EscuadraConContadores>> =
        escuadraDao.getEscuadrasByTirada(tiradaId)

    suspend fun insert(escuadra: Escuadra): Long = withContext(Dispatchers.IO) {
        escuadraDao.insert(escuadra)
    }

    suspend fun getSiguienteNumeroEscuadra(tiradaId: Int): Int = withContext(Dispatchers.IO) {
        escuadraDao.getSiguienteNumeroEscuadra(tiradaId)
    }

    suspend fun delete(escuadra: Escuadra) = withContext(Dispatchers.IO) {
        escuadraDao.delete(escuadra)
    }
}
