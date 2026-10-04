package oscar.plato.core.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.edit
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Informes de errores (Firebase Crashlytics). Solo la versión publicada los envía,
 * y el usuario puede desactivarlos desde el menú lateral.
 */
object InformesDeErrores {

    private const val PREFERENCIAS = "informes_errores"
    private const val CLAVE_ACTIVOS = "activos"

    /** Dato del manifest con el que cada compilación decide si los envía de entrada. */
    private const val META_POR_DEFECTO = "firebase_crashlytics_collection_enabled"

    /** ¿Se envían? Mientras el usuario no lo cambie, lo que diga la compilación. */
    fun activos(contexto: Context): Boolean =
        preferencias(contexto).getBoolean(CLAVE_ACTIVOS, porDefecto(contexto))

    /** Crashlytics recuerda la elección entre arranques; aquí se anota para mostrarla. */
    fun activar(contexto: Context, activos: Boolean) {
        preferencias(contexto).edit { putBoolean(CLAVE_ACTIVOS, activos) }
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(activos)
    }

    private fun porDefecto(contexto: Context): Boolean =
        contexto.packageManager
            .getApplicationInfo(contexto.packageName, PackageManager.GET_META_DATA)
            .metaData?.getBoolean(META_POR_DEFECTO, true) ?: true

    private fun preferencias(contexto: Context) =
        contexto.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
}
