pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PlatoScore"

// :app        -> PlatoScore: gestión de tiradas (organizadores)
// :platostats -> PlatoStats: registro y estadísticas del tirador
// :core       -> código común a las dos (login, cuenta, filtros, fechas, tema)
include(":app")
include(":platostats")
include(":core")
