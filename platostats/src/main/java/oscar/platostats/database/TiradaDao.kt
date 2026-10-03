package oscar.platostats.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import oscar.platostats.models.Serie
import oscar.platostats.models.Tirada
import oscar.platostats.models.TiradaConSeries

@Dao
interface TiradaDao {

    @Insert
    suspend fun insertTirada(tirada: Tirada): Long

    @Insert
    suspend fun insertSeries(series: List<Serie>)

    @Update
    suspend fun updateTirada(tirada: Tirada)

    @Delete
    suspend fun deleteTirada(tirada: Tirada)

    @Query("DELETE FROM series WHERE tiradaId = :tiradaId")
    suspend fun deleteSeriesDe(tiradaId: Int)

    /** Borra todas las tiradas de un usuario; sus series se van en cascada. */
    @Query("DELETE FROM tiradas WHERE userId = :userId")
    suspend fun deleteTiradasDe(userId: String)

    @Transaction
    @Query("SELECT * FROM tiradas WHERE userId = :userId ORDER BY fechaHora DESC")
    fun getAllConSeries(userId: String): LiveData<List<TiradaConSeries>>

    @Transaction
    @Query("SELECT * FROM tiradas WHERE id = :id")
    fun getConSeries(id: Int): LiveData<TiradaConSeries?>

    /** Inserta la tirada y sus series de forma atómica. */
    @Transaction
    suspend fun guardarNueva(tirada: Tirada, series: List<Serie>) {
        val nuevoId = insertTirada(tirada).toInt()
        insertSeries(series.map { it.copy(tiradaId = nuevoId) })
    }

    /** Actualiza la tirada y reemplaza sus series de forma atómica. */
    @Transaction
    suspend fun actualizar(tirada: Tirada, series: List<Serie>) {
        updateTirada(tirada)
        deleteSeriesDe(tirada.id)
        insertSeries(series.map { it.copy(tiradaId = tirada.id) })
    }
}
