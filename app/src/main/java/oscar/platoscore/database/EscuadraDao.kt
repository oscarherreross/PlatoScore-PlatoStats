package oscar.platoscore.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import oscar.platoscore.models.Escuadra
import oscar.platoscore.models.EscuadraConContadores

@Dao
interface EscuadraDao {

    @Insert
    suspend fun insert(escuadra: Escuadra): Long

    @Delete
    suspend fun delete(escuadra: Escuadra)

    @Query(
        "SELECT e.*, " +
                "(SELECT COUNT(*) FROM tiradores t WHERE t.escuadraId = e.id) AS numTiradores " +
                "FROM escuadras e WHERE e.tiradaId = :tiradaId ORDER BY e.numeroEscuadra ASC"
    )
    fun getEscuadrasByTirada(tiradaId: Int): LiveData<List<EscuadraConContadores>>

    @Query("SELECT COALESCE(MAX(numeroEscuadra), 0) + 1 FROM escuadras WHERE tiradaId = :tiradaId")
    suspend fun getSiguienteNumeroEscuadra(tiradaId: Int): Int
}
