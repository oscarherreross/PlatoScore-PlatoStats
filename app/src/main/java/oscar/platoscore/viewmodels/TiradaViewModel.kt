package oscar.platoscore.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import oscar.plato.core.models.FiltroTiradas
import oscar.plato.core.utils.Sesion
import oscar.platoscore.database.PlatoScoreDatabase
import oscar.platoscore.models.ResumenProfesional
import oscar.platoscore.models.ResumenProfesionalCalc
import oscar.platoscore.models.Tirada
import oscar.platoscore.models.TiradaConContadores
import oscar.platoscore.models.TiradorConTirada
import oscar.platoscore.repositories.TiradaRepository

class TiradaViewModel(
    application: Application,
    estado: SavedStateHandle
) : AndroidViewModel(application) {

    private val tiradaRepository: TiradaRepository
    private val uid = Sesion.uid()

    val allTiradas: LiveData<List<TiradaConContadores>>

    /**
     * Filtro de la lista de tiradas. Vive en el SavedStateHandle para que no se
     * pierda cuando Android recrea la pantalla (giro) o cierra el proceso.
     */
    val filtro: MutableLiveData<FiltroTiradas> = estado.getLiveData(CLAVE_FILTRO, FiltroTiradas())

    /** Resumen del profesional (para el menú de perfil), combinando tiradas y tiradores. */
    val resumenProfesional: LiveData<ResumenProfesional>

    init {
        val tiradaDao = PlatoScoreDatabase.getDatabase(application).tiradaDao()
        tiradaRepository = TiradaRepository(tiradaDao)
        allTiradas = tiradaRepository.allTiradas(uid)

        val tiradores = tiradaRepository.tiradoresConTirada(uid)
        resumenProfesional = MediatorLiveData<ResumenProfesional>().apply {
            var tiradasCache: List<TiradaConContadores> = emptyList()
            var tiradoresCache: List<TiradorConTirada> = emptyList()
            fun recalcular() {
                value = ResumenProfesionalCalc.calcular(tiradasCache, tiradoresCache)
            }
            addSource(allTiradas) { tiradasCache = it; recalcular() }
            addSource(tiradores) { tiradoresCache = it; recalcular() }
        }
    }

    fun getTirada(id: Int): LiveData<Tirada?> = tiradaRepository.getTirada(id)

    fun insertTirada(tirada: Tirada) {
        viewModelScope.launch {
            // Se sella con el UID del profesional autenticado para aislar sus datos.
            tiradaRepository.insert(tirada.copy(userId = uid))
        }
    }

    fun updateTirada(tirada: Tirada) {
        viewModelScope.launch {
            tiradaRepository.update(tirada)
        }
    }

    fun deleteTirada(tirada: Tirada) {
        viewModelScope.launch {
            tiradaRepository.delete(tirada)
        }
    }

    private companion object {
        const val CLAVE_FILTRO = "filtro"
    }
}
