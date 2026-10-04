// Top-level build file where you can add configuration options common to all sub-projects/modules.
import java.util.Properties

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

// ─────────────────────────────────────────────────────────────
// Datos legales (política de privacidad y eliminación de cuenta)
// ─────────────────────────────────────────────────────────────
// Se rellenan una sola vez en datos-legales.properties (se lee como UTF-8, no como
// gradle.properties, para admitir tildes). De ahí salen las páginas de docs/ y el
// enlace a la política que muestran las apps. Ver PUBLICACION.md.
val datosLegales: Map<String, String> = run {
    val propiedades = Properties()
    file("datos-legales.properties").reader(Charsets.UTF_8).use { propiedades.load(it) }
    mapOf(
        "responsable" to "NOMBRE_DEL_RESPONSABLE",
        "contacto" to "CORREO_DE_CONTACTO",
        "urlBase" to "URL_DE_LAS_PAGINAS_LEGALES"
    ).mapValues { (clave, marcador) ->
        val valor = propiedades.getProperty(clave).orEmpty().trim()
        // Un dato que conserva su marcador de plantilla cuenta como sin rellenar.
        if (valor == marcador) "" else valor
    }
}
val datosLegalesSinRellenar = datosLegales.filterValues { it.isEmpty() }.keys

// Dirección donde están publicadas las páginas legales (vacía si falta). Las apps
// construyen con ella el enlace a su política de privacidad.
extra["urlPaginasLegales"] = datosLegales.getValue("urlBase").trimEnd('/')

// Genera en docs/ las páginas legales de las dos apps a partir de las plantillas
// de legal/, listas para publicarlas (por ejemplo, con GitHub Pages).
tasks.register<Copy>("generarPaginasLegales") {
    group = "publicacion"
    description = "Genera en docs/ la política de privacidad y la página de eliminación de cuenta."

    val sinRellenar = datosLegalesSinRellenar
    val responsable = datosLegales.getValue("responsable")
    val contacto = datosLegales.getValue("contacto")
    doFirst {
        if (sinRellenar.isNotEmpty()) {
            throw GradleException(
                "Rellena antes en datos-legales.properties: ${sinRellenar.joinToString()}"
            )
        }
    }

    from("legal")
    into("docs")
    filteringCharset = "UTF-8"
    filter { linea: String ->
        linea.replace("{{RESPONSABLE}}", responsable).replace("{{CONTACTO}}", contacto)
    }
}

// Sin los datos legales no se puede generar el paquete que se sube a Google Play
// (bundleRelease). El resto de compilaciones de release solo avisa, para poder
// probar R8 antes de tenerlos.
gradle.taskGraph.whenReady {
    if (datosLegalesSinRellenar.isEmpty()) return@whenReady
    val tareas = allTasks.map { it.name }
    val mensaje = "Faltan datos legales en datos-legales.properties " +
        "(${datosLegalesSinRellenar.joinToString()}). Ver PUBLICACION.md."
    when {
        "bundleRelease" in tareas -> throw GradleException(mensaje)
        "assembleRelease" in tareas -> logger.warn("AVISO: $mensaje")
    }
}
