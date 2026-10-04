// PlatoStats: el tirador registra sus propias tiradas y consulta sus
// estadísticas. Comparte con PlatoScore el código de :core.
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

// Versión de PlatoStats (cada app lleva la suya; ver PUBLICACION.md). El nombre
// es mayor.menor.parche y el código que exige Google Play se calcula a partir de
// él, así que en cada publicación basta con subir uno de estos tres números.
val versionMayor = 1
val versionMenor = 0
val versionParche = 0

// Firma de publicación: se lee de keystore.properties, que está fuera de git. Sin
// ese archivo la variante release se compila igualmente, pero sin firmar.
val keystoreProperties = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}

android {
    namespace = "oscar.platostats"
    compileSdk = 36

    defaultConfig {
        applicationId = "oscar.platostats"
        minSdk = 24
        targetSdk = 36
        versionCode = versionMayor * 10000 + versionMenor * 100 + versionParche
        versionName = "$versionMayor.$versionMenor.$versionParche"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystoreProperties.isNotEmpty()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            // R8: reduce y ofusca el código y descarta los recursos sin usar.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")

            // Crashlytics solo envía informes en la versión publicada, y la tabla que
            // traduce sus trazas ofuscadas (mapping) solo se sube al compilar una
            // release firmada: las de prueba no tocan el proyecto de Firebase.
            manifestPlaceholders["crashlyticsActivo"] = true
            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = keystoreProperties.isNotEmpty()
            }
        }
        debug {
            isMinifyEnabled = false
            manifestPlaceholders["crashlyticsActivo"] = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }

    // La carpeta legal/ va dentro de la app: la política de privacidad se muestra
    // desde ahí, sin depender de que esté publicada en la web.
    sourceSets["main"].assets.srcDir(rootProject.file("legal"))
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

ksp {
    // Esquema de cada versión de la BD de PlatoStats, para escribir y verificar
    // migraciones futuras.
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {

    // Código común (login, cuenta, filtros, tema). Expone también appcompat,
    // material, constraintlayout, activity-ktx y Firebase Auth.
    implementation(project(":core"))

    // ViewModel y LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // CardView
    implementation("androidx.cardview:cardview:1.0.0")

    // DrawerLayout (menú lateral)
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")

    // Room Database (misma versión que en :app)
    val roomVersion = "2.8.4"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
}
