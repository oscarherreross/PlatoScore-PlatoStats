package oscar.platostats.repositories

import androidx.lifecycle.LiveData
import oscar.platostats.database.TiradaPersonalDao
import oscar.platostats.models.SeriePersonal
import oscar.platostats.models.TiradaPersonal
import oscar.platostats.models.TiradaPersonalConSeries

class TiradaPersonalRepository(private val dao: TiradaPersonalDao) {

    fun getAll(userId: String): LiveData<List<TiradaPersonalConSeries>> =
        dao.getAllConSeries(userId)

    fun get(id: Int): LiveData<TiradaPersonalConSeries?> = dao.getConSeries(id)

    suspend fun guardarNueva(tirada: TiradaPersonal, series: List<SeriePersonal>) =
        dao.guardarNueva(tirada, series)

    suspend fun actualizar(tirada: TiradaPersonal, series: List<SeriePersonal>) =
        dao.actualizar(tirada, series)

    suspend fun eliminar(tirada: TiradaPersonal) = dao.deleteTirada(tirada)
}
