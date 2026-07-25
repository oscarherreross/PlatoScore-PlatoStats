package oscar.platoscore.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import oscar.platoscore.models.Tirada
import oscar.platoscore.models.TiradaConContadores
import oscar.platoscore.models.TiradorConTirada

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
                "FROM tiradas t WHERE t.userId = :userId ORDER BY t.fecha DESC"
    )
    fun getAllTiradasConContadores(userId: String): LiveData<List<TiradaConContadores>>

    @Query(
        "SELECT tiradores.*, " +
                "tiradas.id AS t_id, tiradas.userId AS t_userId, tiradas.nombre AS t_nombre, " +
                "tiradas.fecha AS t_fecha, tiradas.precioLocal AS t_precioLocal, " +
                "tiradas.precioGeneral AS t_precioGeneral, tiradas.precioJunior AS t_precioJunior, " +
                "tiradas.precioSenior AS t_precioSenior, tiradas.precioDama AS t_precioDama " +
                "FROM tiradores " +
                "INNER JOIN escuadras ON tiradores.escuadraId = escuadras.id " +
                "INNER JOIN tiradas ON escuadras.tiradaId = tiradas.id " +
                "WHERE tiradas.userId = :userId"
    )
    fun getTiradoresConTirada(userId: String): LiveData<List<TiradorConTirada>>
}
