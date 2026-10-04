# PlatoScore y PlatoStats

Dos aplicaciones Android nativas para el **tiro al plato**, en un mismo repositorio:

| App | Para quién | Qué hace |
|---|---|---|
| **PlatoScore** (`oscar.platoscore`) | El organizador de una tirada | Crea tiradas, las organiza en escuadras con tiradores, anota los platos rotos y genera clasificaciones y recaudación. |
| **PlatoStats** (`oscar.platostats`) | El tirador | Registra sus propias tiradas (lugar, fecha, máquina, tipo, series de 25 platos) y muestra estadísticas de su evolución. |

Las dos comparten el inicio de sesión, la gestión de la cuenta (cambiar la contraseña, eliminarla), la pantalla de carga, el tema y los filtros a través de un módulo común (`:core`). Cada una tiene su propia base de datos y su propio proyecto de Firebase, así que las cuentas son independientes.

La interfaz está en español.

## Funcionalidades

**PlatoScore**
- Tiradas con fecha y precios por categoría (Local, General, Junior, Senior, Dama).
- Escuadras y tiradores, con autocompletado desde el histórico y validación de categorías.
- Cinco clasificaciones por categoría, con empates (posiciones compartidas) y desempate manual.
- Recaudación total y opción de compartir los resultados como texto.
- Filtro por fechas.

**PlatoStats**
- Alta y edición de tiradas con número de series variable (puesto, platos rotos y primer tiro de cada serie).
- Estadísticas: media de platos y de aciertos, mejor tirada, acierto por puesto y por máquina.
- Gráfica de evolución dibujada a mano (sin librerías de gráficos) y opción de compartir las estadísticas.
- Filtros por tipo, máquina y fechas.

## Estructura

```
├── app/          PlatoScore       (oscar.platoscore)
├── platostats/   PlatoStats       (oscar.platostats)
├── core/         Código común     (oscar.plato.core): login, pantalla de carga, cuenta,
│                                  filtros, diálogos, fechas, insets y tema
└── legal/        Política de privacidad y página de eliminación de cuenta de cada app
                  (van dentro de las apps y sirven de plantilla para la web)
```

Cada app implementa la interfaz `PlatoApp` de `:core` en su clase `Application` para indicar su pantalla principal, su logo y su lema. `:core` no conoce a ninguna de las dos apps.

## Tecnología

Kotlin · Views XML con ViewBinding (sin Compose ni Fragments) · MVVM con `AndroidViewModel` y `LiveData` · Room 2.8 con KSP · Firebase Authentication (correo y contraseña) y Crashlytics · Coroutines · Material Components.

`compileSdk` y `targetSdk` 36, `minSdk` 24. Gradle 8.11 (con wrapper), AGP 8.10 y Kotlin 2.0. La variante release se compila con R8.

Los datos se guardan **solo en el dispositivo** (Room, separados por usuario). Firebase se usa únicamente para autenticar y, en la versión publicada, para recibir informes de errores (Crashlytics), que el usuario puede desactivar.

## Cómo compilarlo

### Requisitos
- Android Studio reciente (incluye el JDK 21 con el que se ha probado; hace falta JDK 17 o superior).
- Android SDK con la plataforma 36 (Gradle la descarga si falta).

### 1. Configurar Firebase

Los archivos `google-services.json` **no están en el repositorio**: contienen la configuración de los proyectos de Firebase de cada app. Hay que aportar los propios:

1. En la [consola de Firebase](https://console.firebase.google.com/) crea un proyecto para cada app (o uno solo si prefieres cuentas compartidas).
2. Añade una app Android con el paquete exacto: `oscar.platoscore` para PlatoScore y `oscar.platostats` para PlatoStats.
3. Descarga el `google-services.json` de cada una y colócalo en `app/` y en `platostats/` respectivamente.
4. En **Authentication → Método de acceso** activa **Correo electrónico/contraseña**.

Para solo compilar y ejecutar los tests, sin poder iniciar sesión, basta copiar las plantillas:

```bash
cp app/google-services.json.example app/google-services.json
cp platostats/google-services.json.example platostats/google-services.json
```

### 2. Compilar y probar

```bash
./gradlew :app:assembleDebug          # PlatoScore
./gradlew :platostats:assembleDebug   # PlatoStats
./gradlew testDebugUnitTest           # tests unitarios de los tres módulos
./gradlew lintDebug                   # análisis estático
```

En Windows usa `gradlew.bat` en lugar de `./gradlew`. Si Android Studio no lo hace por ti, crea `local.properties` con la ruta de tu SDK (`sdk.dir=...`).

## Notas para desarrollar

- **`ksp.useKSP2=true` en `gradle.properties` es obligatorio.** Sin él, el procesador de Room 2.8 falla con un `AbstractMethodError` al releer los esquemas exportados.
- Los recursos de `:core` se usan desde las apps con `import oscar.plato.core.R as CoreR`, porque `android.nonTransitiveRClass` está activado.
- Las barras superiores se pintan con `?attr/platoAppBar`, no con `colorPrimary`, para que en modo noche la barra sea grafito y el naranja siga siendo el color de acento.
- La app dibuja de borde a borde (edge-to-edge): cada pantalla nueva debe aplicar sus insets (`InsetsUtil`).
- Cada cambio de base de datos necesita una migración real y su esquema exportado (`app/schemas` y `platostats/schemas`); no se usa `fallbackToDestructiveMigration`.
- Toda pantalla posterior al inicio de sesión empieza su `onCreate` con `if (!exigirSesion()) return`: sin sesión vuelve al login en vez de trabajar con un usuario vacío.
- Los ViewModel que escriben en la base de datos heredan de `GuardadoViewModel` y lanzan sus escrituras con `guardar { ... }`: no se interrumpen si la pantalla se cierra y, si fallan, avisan al usuario en vez de cerrar la app.
- Las pantallas deben conservar su estado al girar: los filtros viven en el `SavedStateHandle` del ViewModel y los diálogos se registran en `DialogosRestaurables` (`:core`), que los reabre con lo que tuvieran escrito. Un diálogo nuevo se abre con `dialogos.mostrar(...)`, no con `.show()`.

[`Summary.md`](Summary.md) recoge el detalle completo: modelo de datos, migraciones, pantallas, decisiones de diseño y limitaciones conocidas.

## Estado

Proyecto en desarrollo, todavía sin publicar en Google Play. [`PUBLICACION.md`](PUBLICACION.md) recoge lo que ya está preparado (firma, versiones, R8, páginas legales) y los pasos que quedan. Solo hay tests unitarios (37) de la lógica de negocio; no hay tests instrumentados.

## Licencia

Todos los derechos reservados. Consulta [`LICENSE`](LICENSE).
