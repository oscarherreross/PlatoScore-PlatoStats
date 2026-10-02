package oscar.platostats.repositories

import androidx.lifecycle.LiveData
import oscar.platostats.database.TiradaDao
import oscar.platostats.models.Serie
import oscar.platostats.models.Tirada
import oscar.platostats.models.TiradaConSeries

class TiradaRepository(private val dao: TiradaDao) {

    fun getAll(userId: String): LiveData<List<TiradaConSeries>> =
        dao.getAllConSeries(userId)

    fun get(id: Int): LiveData<TiradaConSeries?> = dao.getConSeries(id)

    suspend fun guardarNueva(tirada: Tirada, series: List<Serie>) =
        dao.guardarNueva(tirada, series)

    suspend fun actualizar(tirada: Tirada, series: List<Serie>) =
        dao.actualizar(tirada, series)

    suspend fun eliminar(tirada: Tirada) = dao.deleteTirada(tirada)
}
