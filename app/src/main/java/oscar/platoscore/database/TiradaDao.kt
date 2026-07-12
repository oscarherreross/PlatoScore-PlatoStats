package oscar.platoscore.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import oscar.platoscore.models.Tirada

@Dao
interface TiradaDao {

    @Insert
    suspend fun insert(tirada: Tirada): Long

    @Update
    suspend fun update(tirada: Tirada)

    @Delete
    suspend fun delete(tirada: Tirada)

    @Query("SELECT * FROM tiradas WHERE id = :id")
    fun getTirada(id: Int): LiveData<Tirada>

    @Query("SELECT * FROM tiradas ORDER BY fecha DESC")
    fun getAllTiradas(): LiveData<List<Tirada>>

    @Query("DELETE FROM tiradas WHERE id = :id")
    suspend fun deleteTiradaById(id: Int)
}
