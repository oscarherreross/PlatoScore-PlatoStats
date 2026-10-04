package oscar.plato.core.viewmodels

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import oscar.plato.core.R

/**
 * Corrutinas que duran lo que la app, para lo que no debe interrumpirse cuando se
 * cierra la pantalla que lo empezó.
 */
internal val alcanceDeApp = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

/**
 * ViewModel que escribe en la base de datos. Sus escrituras se lanzan con [guardar]:
 *  - no se interrumpen aunque la pantalla se cierre a mitad (guardar al salir,
 *    guardar y volver atrás);
 *  - si fallan, el error se registra y se avisa al usuario, en vez de cerrar la app.
 */
abstract class GuardadoViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Lanza una escritura. [alTerminar] recibe si ha salido bien, para las pantallas
     * que esperan al resultado antes de seguir.
     */
    protected fun guardar(alTerminar: (Boolean) -> Unit = {}, operacion: suspend () -> Unit) {
        alcanceDeApp.launch {
            val hecho = try {
                operacion()
                true
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                FirebaseCrashlytics.getInstance().recordException(error)
                // El aviso no depende de la pantalla: puede haberse cerrado ya.
                Toast.makeText(getApplication(), R.string.error_guardar, Toast.LENGTH_LONG).show()
                false
            }
            alTerminar(hecho)
        }
    }
}
