package oscar.platoscore.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import oscar.platoscore.models.Tirador

@Dao
interface TiradorDao {

    @Insert
    suspend fun insert(tirador: Tirador): Long

    @Update
    suspend fun update(tirador: Tirador)

    @Delete
    suspend fun delete(tirador: Tirador)

    @Query("SELECT * FROM tiradores WHERE escuadraId = :escuadraId ORDER BY id ASC")
    fun getTiradores(escuadraId: Int): LiveData<List<Tirador>>

    /** Histórico completo, del más reciente al más antiguo (para autocompletar). */
    @Query("SELECT * FROM tiradores ORDER BY id DESC")
    fun getAllTiradores(): LiveData<List<Tirador>>

    @Query(
        "SELECT tiradores.* FROM tiradores " +
                "INNER JOIN escuadras ON tiradores.escuadraId = escuadras.id " +
                "WHERE escuadras.tiradaId = :tiradaId " +
                "ORDER BY tiradores.platosRotos DESC"
    )
    fun getTiradoresByTirada(tiradaId: Int): LiveData<List<Tirador>>
}
