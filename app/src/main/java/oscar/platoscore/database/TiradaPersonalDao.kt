package oscar.platoscore.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import oscar.platoscore.models.SeriePersonal
import oscar.platoscore.models.TiradaPersonal
import oscar.platoscore.models.TiradaPersonalConSeries

@Dao
interface TiradaPersonalDao {

    @Insert
    suspend fun insertTirada(tirada: TiradaPersonal): Long

    @Insert
    suspend fun insertSeries(series: List<SeriePersonal>)

    @Update
    suspend fun updateTirada(tirada: TiradaPersonal)

    @Delete
    suspend fun deleteTirada(tirada: TiradaPersonal)

    @Query("DELETE FROM series_personales WHERE tiradaPersonalId = :tiradaPersonalId")
    suspend fun deleteSeriesDe(tiradaPersonalId: Int)

    @Transaction
    @Query("SELECT * FROM tiradas_personales WHERE userId = :userId ORDER BY fechaHora DESC")
    fun getAllConSeries(userId: String): LiveData<List<TiradaPersonalConSeries>>

    @Transaction
    @Query("SELECT * FROM tiradas_personales WHERE id = :id")
    fun getConSeries(id: Int): LiveData<TiradaPersonalConSeries?>

    /** Inserta la tirada y sus series de forma atómica. */
    @Transaction
    suspend fun guardarNueva(tirada: TiradaPersonal, series: List<SeriePersonal>) {
        val nuevoId = insertTirada(tirada).toInt()
        insertSeries(series.map { it.copy(tiradaPersonalId = nuevoId) })
    }

    /** Actualiza la tirada y reemplaza sus series de forma atómica. */
    @Transaction
    suspend fun actualizar(tirada: TiradaPersonal, series: List<SeriePersonal>) {
        updateTirada(tirada)
        deleteSeriesDe(tirada.id)
        insertSeries(series.map { it.copy(tiradaPersonalId = tirada.id) })
    }
}
