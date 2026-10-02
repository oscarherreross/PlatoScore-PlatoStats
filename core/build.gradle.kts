// Código común a PlatoScore y PlatoStats: inicio de sesión, cuenta, pantalla de
// carga, filtros, fechas, insets y el tema Grafito.
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "oscar.plato.core"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // Base de UI. Se expone con api porque las dos apps la usan directamente.
    api("androidx.core:core-ktx:1.13.1")
    api("androidx.appcompat:appcompat:1.7.0")
    api("com.google.android.material:material:1.12.0")
    api("androidx.constraintlayout:constraintlayout:2.1.4")
    api("androidx.activity:activity-ktx:1.9.2")

    // Firebase (autenticación). Cada app aporta su propio google-services.json.
    api(platform("com.google.firebase:firebase-bom:33.1.2"))
    api("com.google.firebase:firebase-auth-ktx")

    // Testing
    testImplementation("junit:junit:4.13.2")
}
