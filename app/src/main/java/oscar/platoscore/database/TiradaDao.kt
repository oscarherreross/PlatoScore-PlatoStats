package oscar.platoscore.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import oscar.platoscore.models.Tirada
import oscar.platoscore.models.TiradaConContadores

@Dao
interface TiradaDao {

    @Insert
    suspend fun insert(tirada: Tirada): Long

    @Update
    suspend fun update(tirada: Tirada)

    @Delete
    suspend fun delete(tirada: Tirada)

    @Query("SELECT * FROM tiradas WHERE id = :id")
    fun getTirada(id: Int): LiveData<Tirada?>

    @Query(
        "SELECT t.*, " +
                "(SELECT COUNT(*) FROM escuadras e WHERE e.tiradaId = t.id) AS numEscuadras, " +
                "(SELECT COUNT(*) FROM tiradores ti " +
                "   INNER JOIN escuadras e ON ti.escuadraId = e.id " +
                "   WHERE e.tiradaId = t.id) AS numTiradores " +
                "FROM tiradas t ORDER BY t.fecha DESC"
    )
    fun getAllTiradasConContadores(): LiveData<List<TiradaConContadores>>
}
