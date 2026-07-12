package oscar.platoscore.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import oscar.platoscore.models.Escuadra

@Dao
interface EscuadraDao {

    @Insert
    suspend fun insert(escuadra: Escuadra): Long

    @Delete
    suspend fun delete(escuadra: Escuadra)

    @Query("SELECT * FROM escuadras WHERE tiradaId = :tiradaId ORDER BY numeroEscuadra ASC")
    fun getEscuadrasByTirada(tiradaId: Int): LiveData<List<Escuadra>>

    @Query("SELECT COALESCE(MAX(numeroEscuadra), 0) + 1 FROM escuadras WHERE tiradaId = :tiradaId")
    suspend fun getSiguienteNumeroEscuadra(tiradaId: Int): Int
}
