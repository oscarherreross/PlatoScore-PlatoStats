package oscar.plato.core.ui

import android.app.Dialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Hace que los diálogos de una pantalla sobrevivan a su recreación (giro, cambio
 * de tema, vuelta tras cerrarse el proceso) sin recurrir a Fragments.
 *
 * La pantalla indica en su onCreate cómo se construye cada diálogo ([registrar])
 * y lo abre con [mostrar]. Al guardarse el estado de la pantalla se anotan qué
 * diálogo está abierto, sus argumentos y su contenido (lo escrito en sus campos);
 * al recrearse, [registrar] lo vuelve a abrir tal como estaba.
 *
 * Los argumentos hacen también de estado: lo que un diálogo deba recordar y no
 * esté en sus vistas (una fecha elegida, por ejemplo) lo lee de ellos al
 * construirse y lo va anotando en ellos.
 *
 * Solo hay un diálogo abierto a la vez. Los selectores de fecha que se abren desde
 * otro diálogo van con [mostrarDePaso]: no se reabren, pero se cierran a tiempo.
 */
class DialogosRestaurables(private val activity: ComponentActivity) {

    private class Abierto(val clave: String, val args: Bundle, val dialogo: Dialog)

    private val constructores = HashMap<String, (Bundle) -> Dialog>()
    private var abierto: Abierto? = null
    private val dePaso = ArrayList<Dialog>()

    /** Lo que había abierto antes de recrearse la pantalla. Solo se puede leer una vez. */
    private var guardado: Bundle? = null
    private var guardadoLeido = false

    init {
        activity.savedStateRegistry.registerSavedStateProvider(CLAVE_ESTADO) { guardar() }
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            // Un diálogo abierto no puede sobrevivir a la ventana de su pantalla.
            override fun onDestroy(owner: LifecycleOwner) = cerrarTodos()
        })
    }

    /**
     * Indica cómo se construye (sin mostrarlo) el diálogo [clave] a partir de sus
     * argumentos. Hay que llamarlo en onCreate, después de super.onCreate y con la
     * pantalla ya montada: si ese diálogo estaba abierto cuando se recreó la
     * pantalla, se vuelve a abrir aquí mismo.
     */
    fun registrar(clave: String, constructor: (Bundle) -> Dialog) {
        constructores[clave] = constructor

        val estado = estadoGuardado() ?: return
        if (estado.getString(ESTADO_CLAVE) != clave) return
        guardado = null

        val dialogo = construir(clave, estado.getBundle(ESTADO_ARGS) ?: Bundle())
        // Le devuelve su contenido y, de paso, lo muestra.
        estado.getBundle(ESTADO_DIALOGO)?.let { dialogo.onRestoreInstanceState(it) }
        if (!dialogo.isShowing) dialogo.show()
    }

    /** Abre el diálogo registrado como [clave], en lugar del que hubiera abierto. */
    fun mostrar(clave: String, args: Bundle = Bundle()) {
        abierto?.dialogo?.dismiss()
        construir(clave, args).show()
    }

    /** Abre un diálogo que no se reabre al recrearse la pantalla (un selector de fecha). */
    fun mostrarDePaso(dialogo: Dialog) {
        dePaso.removeAll { !it.isShowing }
        dePaso.add(dialogo)
        dialogo.show()
    }

    private fun construir(clave: String, args: Bundle): Dialog {
        val constructor = checkNotNull(constructores[clave]) { "Diálogo sin registrar: $clave" }
        return constructor(args).also { abierto = Abierto(clave, args, it) }
    }

    private fun estadoGuardado(): Bundle? {
        if (!guardadoLeido) {
            guardadoLeido = true
            guardado = activity.savedStateRegistry.consumeRestoredStateForKey(CLAVE_ESTADO)
                // Tras cerrarse el proceso, los argumentos se releen con las clases de la app.
                ?.also { it.classLoader = activity.classLoader }
        }
        return guardado
    }

    private fun guardar(): Bundle {
        val estado = Bundle()
        val actual = abierto?.takeIf { it.dialogo.isShowing } ?: return estado
        estado.putString(ESTADO_CLAVE, actual.clave)
        estado.putBundle(ESTADO_ARGS, actual.args)
        estado.putBundle(ESTADO_DIALOGO, actual.dialogo.onSaveInstanceState())
        return estado
    }

    private fun cerrarTodos() {
        abierto?.dialogo?.dismiss()
        dePaso.forEach { it.dismiss() }
    }

    private companion object {
        const val CLAVE_ESTADO = "oscar.plato.core.dialogos"
        const val ESTADO_CLAVE = "clave"
        const val ESTADO_ARGS = "args"
        const val ESTADO_DIALOGO = "dialogo"
    }
}
