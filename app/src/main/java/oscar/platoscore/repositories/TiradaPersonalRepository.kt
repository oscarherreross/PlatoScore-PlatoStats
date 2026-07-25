package oscar.platoscore.repositories

import androidx.lifecycle.LiveData
import oscar.platoscore.database.TiradaPersonalDao
import oscar.platoscore.models.SeriePersonal
import oscar.platoscore.models.TiradaPersonal
import oscar.platoscore.models.TiradaPersonalConSeries

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
