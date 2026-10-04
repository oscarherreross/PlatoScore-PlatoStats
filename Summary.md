# PlatoScore y PlatoStats — Documento de traspaso

## 1. Qué son las apps

Dos apps **Android nativas** de **tiro al plato**, en un mismo repositorio. Hasta octubre de 2026 eran una sola app (PlatoScore) con dos *roles* que se elegían al abrir; se separaron en dos productos con las mismas funcionalidades:

- **PlatoScore** (organizador): crea *tiradas* (eventos), las organiza en *escuadras* con *tiradores*, anota platos rotos y genera **clasificaciones** y **recaudación**.
- **PlatoStats** (tirador): registra *sus propias participaciones* en tiradas (lugar, fecha/hora, máquina, tipo, series de 25 platos) y ve **estadísticas** de su evolución.

| App | Módulo | Paquete / applicationId |
|---|---|---|
| PlatoScore | `:app` | `oscar.platoscore` (el de siempre: se actualiza encima de instalaciones previas) |
| PlatoStats | `:platostats` | `oscar.platostats` |
| *(código común)* | `:core` | `oscar.plato.core` (librería) |

Todo está en español (textos en `strings.xml`).

## 2. Stack tecnológico (versiones reales)

- **Lenguaje**: Kotlin. **UI**: Views XML clásicas con **ViewBinding** (NO Jetpack Compose, NO dataBinding, NO Fragments — todo son **Activities**).
- **SDK**: `compileSdk`/`targetSdk` **36** (Android 16, lo que exige Google Play a las apps nuevas desde agosto de 2026), `minSdk` **24**, Java 11.
- **Versión y release**: las dos apps están en la **1.0.0** y se numeran por separado; el `versionCode` se calcula a partir de `versionMayor/Menor/Parche` (al principio de cada `build.gradle.kts`). La variante release usa **R8** (`isMinifyEnabled` + `isShrinkResources`) y se firma con `keystore.properties` (fuera de git) si existe. Ver `PUBLICACION.md`.
- **Proyecto multi-módulo**: `:app` y `:platostats` (aplicaciones) dependen de `:core` (librería Android). `:core` expone con `api` appcompat, material, constraintlayout, activity-ktx y Firebase Auth.
- **Arquitectura**: **MVVM** → Activities → ViewModels (`AndroidViewModel` + `LiveData`) → Repositories → DAOs.
- **Persistencia**: **Room 2.8.4** (SQLite local), compilado con **KSP** (no kapt). **Cada app tiene su propia BD** (ver §4), con `exportSchema=true` y migraciones reales (sin `fallbackToDestructiveMigration`).
- **Autenticación**: **Firebase Authentication** (email/contraseña). Firebase BoM **33.1.2**, `firebase-auth-ktx`, plugin `com.google.gms.google-services`. **Cada app tiene su propio proyecto Firebase** (ver §5).
- **Informes de errores**: **Firebase Crashlytics** (dependencia en `:core`, plugin `com.google.firebase.crashlytics` en cada app). Solo envía en la versión release (`firebase_crashlytics_collection_enabled` se fija por `manifestPlaceholders`), el usuario puede desactivarlo en el menú lateral (`InformesDeErrores`) y la tabla de R8 solo se sube a Firebase al compilar una release firmada. No se asocia ningún identificador de usuario a los informes.
- **Async**: Coroutines 1.8.1 (los repositorios usan `withContext(Dispatchers.IO)`). Las escrituras de los ViewModel se lanzan con `GuardadoViewModel.guardar`, que no usa `viewModelScope` para que cerrar la pantalla no las interrumpa.
- **Librerías AndroidX**: appcompat 1.7.0, material 1.12.0, constraintlayout 2.1.4, activity-ktx 1.9.2, lifecycle 2.8.7, recyclerview 1.3.2, cardview 1.0.0, **drawerlayout 1.2.0**.
- **Tests**: JUnit4 unitarios (lógica pura) en los tres módulos. NO hay tests instrumentados/Espresso.
- **Sin librerías de gráficas externas**: la gráfica de estadísticas de PlatoStats es una **View custom dibujada a mano** (`LineChartView`).

**Gotchas críticos de build:**
- **Kotlin 2.2.21 y KSP 2.3.11 van emparejados** (`gradle/libs.versions.toml`). KSP tiene que ser **2.3.6 o posterior**: las versiones anteriores de KSP2 no cerraban los `.jar` que leían y el demonio de Gradle se quedaba con el `classes.jar` de `:core` abierto, así que en Windows la compilación siguiente a un cambio en `:core` fallaba en `:core:bundleLibCompileToJarDebug` («el archivo está siendo utilizado por otro proceso»). KSP 2.3.x exige Kotlin 2.2 o posterior y, desde la 2.3.12, AGP 8.12 o posterior. Ya no existe `ksp.useKSP2`: KSP 2.3 solo tiene KSP2, que es el que necesita el procesador de Room 2.8 (con KSP1 cascaba con `AbstractMethodError` al releer los esquemas exportados).
- `android.nonTransitiveRClass=true`: la `R` de cada módulo solo contiene sus propios recursos. El código de las apps se refiere a los recursos de `:core` con `import oscar.plato.core.R as CoreR` (`CoreR.string.accion_cancelar`, `CoreR.menu.menu_filtro`, `CoreR.id.action_filtros`…). Los XML no necesitan nada especial.

## 3. Estructura del código

```
:core   oscar.plato.core
├── PlatoApp           Interfaz que implementa la Application de cada app (pantalla principal, logo, lema)
├── models/            FiltroTiradas (genérico), DatosLegales
├── ui/                SplashActivity, LoginActivity, CuentaUi (cambiar contraseña / cerrar sesión /
│                      eliminar cuenta), FiltrosDialog (+ OpcionFiltro), DialogosRestaurables,
│                      PoliticaPrivacidadActivity (política dentro de la app, en un WebView),
│                      SesionRequerida (exigirSesion)
├── viewmodels/        CuentaViewModel (operaciones de cuenta contra Firebase),
│                      GuardadoViewModel (base de los ViewModel que escriben en la BD)
├── utils/             Fechas, Sesion, InformesDeErrores, InsetsUtil, EdgeToEdgeExt
└── res/               Tema Theme.Plato, paleta Grafito, attrs (platoAppBar), textos comunes,
                       layouts de login / splash / diálogos de filtros y contraseña

:app (PlatoScore)   oscar.platoscore
├── PlatoScoreApp      Application → PlatoApp
├── database/          PlatoScoreDatabase + DAOs (Tirada, Escuadra, Tirador)
├── models/            Entidades + POJOs + lógica pura (Clasificacion, ResumenProfesional…) + acepta() del filtro
├── repositories/ · viewmodels/
├── ui/activities/     MainActivity, TiradaDetailActivity, EscuadraDetailActivity, ResultadosActivity
├── ui/adapters/       Tirada, Escuadra, Tirador, Resultado
└── utils/Extras

:platostats (PlatoStats)   oscar.platostats
├── PlatoStatsApp      Application → PlatoApp
├── database/          PlatoStatsDatabase + TiradaDao
├── models/            Tirada, Serie, TiradaConSeries, Estadisticas, ResumenUsuario/PerfilUsuario + acepta() del filtro
├── repositories/ · viewmodels/
├── ui/activities/     MainActivity, TiradaDetailActivity, EstadisticasActivity
├── ui/adapters/       TiradaAdapter
├── ui/views/          LineChartView (gráfica custom)
├── ui/Filtros         Diálogo de filtros con tipo + máquina (envuelve FiltrosDialog)
└── utils/Extras
```

En la raíz: `legal/` (páginas legales de cada app y `datos-legales.properties`, con el responsable y el contacto) y `PUBLICACION.md` (guía de publicación). La carpeta `legal/` entera se incluye en las dos apps como *assets*.

**Regla:** nada de `:core` puede conocer clases de una app concreta; lo que varía por app pasa por `PlatoApp` (pantalla principal, logo, lema, carpeta de sus páginas legales y borrado de los datos de un usuario). Las dos apps tienen clases con el mismo nombre (`MainActivity`, `Tirada`, `TiradaDao`…) en paquetes distintos: no hay conflicto porque nunca se compilan juntas.

## 4. Modelo de datos (Room)

### PlatoScore — `platoscore_database`, **versión 7** (esquemas en `app/schemas/`)
- **`Tirada`** (evento): `id`, `userId` (UID del dueño), `nombre`, `fecha` (**ISO `yyyy-MM-dd`**), 5 precios (`precioLocal/General/Junior/Senior/Dama`). Método Kotlin `precioPara(tirador)` = mínimo de las categorías marcadas, o General.
- **`Escuadra`**: `id`, `tiradaId` (FK→Tirada, CASCADE), `numeroEscuadra`.
- **`Tirador`**: `id`, `escuadraId` (FK→Escuadra, CASCADE), `nombreApellidos`, `dni`, `numeroLicencia`, `platosRotos`, flags `esLocal/esJunior/esSenior/esDama`, `ordenDesempate`. **El precio NO se persiste** (se calcula al vuelo).
- POJOs/lógica: `TiradaConContadores`, `EscuadraConContadores`, `TiradorConTirada`, `Resultado`, `Clasificacion`, `ResumenProfesional`.

**Migraciones (todas reales, preservan datos):**
- `1→2`: fechas `dd/MM/yyyy`→ISO; elimina `tiradores.precio`; añade índice en `escuadraId`.
- `2→3`: añade `tiradores.ordenDesempate`.
- `3→4`: añade `tiradas.userId`; crea `tiradas_personales` + `series_personales`.
- `4→5`: recrea tablas personales (añade `tipo`, quita `puestoInicial`, añade `serie.puesto`) con **patrón de tablas de respaldo SIN claves foráneas** (para no disparar borrado en cascada ni depender de `DROP COLUMN`).
- `5→6`: añade `tiradas_personales.maquina`.
- `6→7`: **elimina `series_personales` y `tiradas_personales`** (el modo personal pasó a PlatoStats). Sus datos no se trasladaron: la app no estaba publicada.

Las migraciones `3→4`…`5→6` siguen mencionando las tablas personales porque son los nombres reales que tuvieron en esta BD: no hay que tocarlas.

### PlatoStats — `platostats_database`, **versión 1** (esquemas en `platostats/schemas/`)
- **`Tirada`** (tabla `tiradas`): `id`, `userId`, `lugar`, `fechaHora` (**Long epoch millis**), `numeroEscuadra`, `tipo` (`"competicion"`/`"entrenamiento"`), `maquina` (`"robot"`/`"trap"`/`"olimpico"`), `notas`.
- **`Serie`** (tabla `series`): `id`, `tiradaId` (FK→Tirada, CASCADE), `numeroSerie`, `puesto`, `platosRotos`, `platosPrimerTiro` (nullable). Constante `PLATOS_POR_SERIE = 25`.
- POJOs/lógica: `TiradaConSeries`, `Estadisticas` (+`ResumenEstadisticas`/`AciertoPorPuesto`/`AciertoPorMaquina`/`PuntoEvolucion`), `ResumenUsuario`/`PerfilUsuario`.

Es el mismo esquema que tenían esas tablas en la v6 de PlatoScore, con los nombres sin "personal". Los cambios futuros deben ir con migraciones reales, como en PlatoScore.

**Convención de migración importante:** para quitar columnas se usa el patrón de recrear tablas volcando a respaldos sin FK. Añadir columna `NOT NULL DEFAULT x` en la migración es seguro aunque la entidad no declare default, porque Room solo compara defaults si la entidad usa `@ColumnInfo(defaultValue=...)`.

## 5. Autenticación (¡importante!)

- **Proyectos Firebase separados**: PlatoScore usa el proyecto **"platoscore"** (`app/google-services.json`); PlatoStats usa el proyecto **"platostats"** (`platostats/google-services.json`). **Los dos archivos reales NO están en git** (están en el `.gitignore`); en el repositorio solo van las plantillas `app/google-services.json.example` y `platostats/google-services.json.example`, con valores falsos. **Las cuentas son independientes**: quien use las dos apps se registra en cada una.
- **Ya no hay roles** ni pantalla de selección de rol: cada app es un solo modo.
- **Aislamiento de datos por usuario**: cada `Tirada` (en las dos apps) lleva `userId` (UID de Firebase) y todas las consultas filtran por él (`Sesion.uid()`).
- **Sesión obligatoria**: toda pantalla posterior al login empieza su `onCreate` con `if (!exigirSesion()) return`. Sin usuario vuelve al inicio de sesión en vez de consultar con un UID vacío (que en PlatoScore enseñaría las tiradas sin dueño); con la pantalla a la vista, un `AuthStateListener` hace lo mismo si la sesión se pierde. Además, una vez por arranque se comprueba con Firebase (`reload()`) que la cuenta sigue siendo válida: si se eliminó o cambió de contraseña en otro dispositivo, se cierra la sesión. Sin conexión no se hace nada.
- **NO hay sincronización en la nube**: los datos viven en Room **local del dispositivo**, particionados por UID. Firebase se usa SOLO para autenticar y para los informes de errores.
- **Eliminar cuenta** (menú lateral, exigido por Google Play): pide la contraseña, reautentica, llama a `FirebaseUser.delete()` y después borra del dispositivo las tiradas de ese UID (`PlatoApp.borrarDatosDe`; el resto cae en cascada). Lo hace `CuentaViewModel`, para que la operación siga su curso aunque la pantalla se recree.
- **Copia de seguridad de Android**: solo se copia la base de datos (`backup_rules.xml` y `data_extraction_rules.xml` de cada app), no la sesión de Firebase. En PlatoScore, que guarda DNI de terceros, a la nube solo sube si la copia va cifrada.
- Setup requerido en cada consola Firebase: proveedor **Email/Password habilitado** y el `google-services.json` real en la carpeta del módulo. **Sin él, ese módulo no compila.** Quien clone el repositorio debe descargarlo de su consola (Configuración del proyecto → General → Tus apps → la app Android → `google-services.json`) o, solo para compilar sin poder iniciar sesión, copiar la plantilla `.example` como `google-services.json`. Como no está en git, conviene guardar una copia aparte.
- Sin su `google-services.json` un módulo **no compila**, y no se puede saltar la tarea de Firebase (`-x processDebugGoogleServices` rompe `mergeDebugResources`). Para verificar sin el archivo real, compilar en una **copia aparte** del repositorio (un `git clone` en otra carpeta) usando las plantillas `.example`. **Nunca** sustituir el de la carpeta del proyecto por uno de prueba: es la configuración real y, al no estar en git, no se puede recuperar de ahí.

## 6. Pantallas y flujos

**Comunes (`:core`):**
- **`SplashActivity`** (launcher de las dos apps, declarada en el manifest de cada una): logo, nombre y lema de la app en curso sobre el naranja de marca durante ~1,3 s; luego entra en la pantalla principal si hay sesión, o en el login si no. El fondo se pinta desde `windowBackground` (`Theme.Plato.Splash`) para evitar el parpadeo blanco.
- **`LoginActivity`**: email+contraseña, alterna iniciar sesión / crear cuenta, recuperación por correo, errores traducidos, enlace a la política de privacidad. Muestra el nombre de la app en curso. Sin barra superior.
- Cerrar sesión y eliminar la cuenta (`CuentaUi`) vuelven al login vaciando la pila.

**PlatoScore:**
- **`MainActivity`**: lista de tiradas + menú lateral de perfil. FAB crea tirada (diálogo nombre+fecha con DatePicker). Pulsación larga = eliminar.
- **`TiradaDetailActivity`**: edita nombre/fecha/precios (autoguardado en `onPause`), lista escuadras (numeración `MAX(numeroEscuadra)+1`), botón "Generar resultados".
- **`EscuadraDetailActivity`**: tiradores de la escuadra; alta/edición por diálogo con **autocompletado desde el histórico**, validación (Junior/Senior excluyentes por edad).
- **`ResultadosActivity`**: 5 clasificaciones (Local/General/Junior/Senior/Dama) por platos rotos desc, **empates** (posiciones compartidas 1,2,2,4 + desempate manual con `ordenDesempate`), recaudación total, y **"Compartir resultados"** como texto (`ACTION_SEND`).

**PlatoStats:**
- **`MainActivity`**: lista de tiradas + menú lateral de perfil. FAB → alta.
- **`TiradaDetailActivity`**: formulario con lugar, fecha/hora (Date+Time picker), escuadra, selector tipo (radio), selector máquina (radio), **nº de series dinámico** (cada serie: puesto, platos rotos, 1.er tiro opcional), notas. Validaciones.
- **`EstadisticasActivity`**: resumen (nº tiradas, media platos, media aciertos, mejor, media 1.er tiro), **media por puesto**, **media por máquina**, y **gráfica de evolución** (último elemento), botón **"Compartir estadísticas"** como texto.

## 7. UI: convenciones y detalles técnicos

- **Toolbar en todas las pantallas** (tema base `Theme.Plato` con parent **`NoActionBar`**), NO el ActionBar del sistema. Cada Activity hace `setSupportActionBar(binding.toolbar)`. Pantallas de detalle con flecha atrás (`onSupportNavigateUp`→`finish`). Login y splash sin barra.
- **Paleta Grafito** (en `:core`, compartida): naranja del plato como primario + grafito como secundario. Tokens semánticos en `colors.xml` (`naranja`, `naranja_oscuro`, `naranja_claro`, `naranja_acento`, `grafito`, `grafito_oscuro`, `grafito_claro`, `crema`). Día = barra naranja; noche = **Grafito nocturno** (barra grafito + naranja como acento).
- **Barras con `?attr/platoAppBar`, no con `colorPrimary`**: las toolbars y cabeceras del drawer se pintan con el atributo propio `platoAppBar` (naranja de día, `grafito_oscuro` de noche) y su texto con `?attr/platoOnAppBar`. Así el naranja sigue siendo el acento (botones, enlaces, línea principal de la gráfica) también de noche. **Toda pantalla nueva debe pintar su toolbar con `?attr/platoAppBar`.**
- **Iconos**: adaptativos (`VectorDrawable` de fondo + primer plano dentro de la zona segura) con respaldos `webp` para API 24–25. PlatoScore: monograma **«P»** con el plato en el ojo de la letra. PlatoStats: **gráfica de evolución** que sube y termina en el plato. El logo de la splash es `ic_logo` de cada app.
- **Menú lateral (drawer)** en la pantalla principal de cada app: `DrawerLayout` con hamburguesa a la izquierda; cabecera con email + resumen (PlatoScore = tiradas/escuadras/tiradores/recaudación; PlatoStats = platos disparados/tiros/aciertos por tipo). Acciones compartidas vía **`CuentaUi`** (cambiar contraseña con reautenticación; cerrar sesión; política de privacidad; eliminar cuenta). Todo el contenido del menú se desplaza, porque en horizontal no cabe. El menú se **superpone** a la barra (el toolbar va dentro del DrawerLayout).
- **Edge-to-edge activado** en todas las pantallas (`enableEdgeToEdge`/helper `enableEdgeToEdgeConToolbar` para iconos de barra de estado claros sobre la barra). Gestión de *insets* con **`InsetsUtil`** (toolbar padTop; listas/botones/FAB respetan la barra de navegación). En pantallas con drawer, el listener de insets va **en el `DrawerLayout`** porque intercepta los insets antes que sus hijos. Desde `targetSdk 35` el borde a borde es obligatorio. Regla: **toda pantalla nueva debe aplicar sus insets** o el contenido quedará bajo las barras.
- **Estado al girar** (o al recrearse la pantalla por cualquier otro motivo): los **filtros** viven en el `SavedStateHandle` de `TiradaViewModel` (`filtro`); los **diálogos** se registran en el `onCreate` de su pantalla con **`DialogosRestaurables`** (`:core`), que guarda cuál está abierto, sus argumentos y lo escrito en él, y lo reabre. Lo que un diálogo deba recordar y no esté en sus vistas (una fecha elegida) va en sus argumentos; las entidades que viajan en ellos son `Parcelable` (`@Parcelize`). Los selectores de fecha abiertos desde otro diálogo no se reabren (`mostrarDePaso`). El formulario de PlatoStats guarda sus filas de series en `onSaveInstanceState`, porque se crean por código y comparten id. **Todo diálogo nuevo debe abrirse con `dialogos.mostrar(...)`, no con `.show()`.**
- **Filtros**: agrupados tras un **botón de embudo** en la barra que abre un **diálogo** (`FiltrosDialog`). `FiltroTiradas` es genérico (en `:core`) y cada app define su extensión `acepta(...)`: PlatoScore filtra solo por fechas (sus tiradas no tienen tipo ni máquina); PlatoStats también por tipo y máquina, cuyas opciones pasa `ui/Filtros` al diálogo.
- **Fechas**: utilidad `Fechas` centraliza formatos. PlatoScore persiste ISO `yyyy-MM-dd` (para que `ORDER BY fecha` sea cronológico); PlatoStats usa epoch millis.
- **Compartir** (las dos apps): genera texto plano y abre `Intent.createChooser` (`ACTION_SEND`, `text/plain`). Respeta el filtro activo en estadísticas.

## 8. Tests

Unitarios (JUnit4, lógica pura, sin Android), 37 en total:
- `:core`: `FechasTest`, `FiltroTiradasTest`.
- `:app`: `ClasificacionTest`, `FiltroTiradasTest`, `ResumenProfesionalTest`.
- `:platostats`: `EstadisticasTest`, `FiltroTiradasTest`, `PerfilUsuarioTest`.

Se ejecutan con `./gradlew testDebugUnitTest` (todos los módulos; `:platostats` necesita su `google-services.json`) o por módulo (`./gradlew :app:testDebugUnitTest`). La lógica de negocio (clasificaciones, precios, estadísticas, filtros, fechas) está aislada en `models`/`utils` justo para poder testearla.

## 9. Decisiones clave a respetar

1. **KSP 2.3.6 o posterior**, actualizado a la vez que Kotlin (ver los gotchas de build de §2).
2. **Precio del tirador nunca persistido** — siempre `Tirada.precioPara()`.
3. **Fechas de PlatoScore en ISO**; de PlatoStats en epoch millis.
4. **Migraciones reales** con esquemas exportados; patrón de respaldo-sin-FK para quitar columnas.
5. **compileSdk/targetSdk 36**, el nivel que exige Google Play. Subirlo cuando Play lo pida, y volver a probar las dos apps al hacerlo.
6. **Todas las pantallas con Toolbar propio** (no ActionBar del sistema), pintado con `?attr/platoAppBar`.
7. **Todo texto en `strings.xml`** (lo común en `:core`, lo propio en cada app); extras de Intent en `utils/Extras` de cada app.
8. **`:core` no conoce a ninguna app**: lo específico de cada una entra por `PlatoApp`.
9. **Las pantallas conservan su estado al girar**: filtros en `SavedStateHandle` y diálogos con `DialogosRestaurables`, sin Fragments.
10. **Las escrituras no cierran la app ni se quedan a medias**: los ViewModel heredan de `GuardadoViewModel` y escriben con `guardar { ... }`. Si fallan, se registra el error en Crashlytics y se avisa con un mensaje; el formulario de PlatoStats, además, espera al resultado y no se cierra si falla.
11. **Si cambia lo que la app envía fuera del dispositivo**, hay que actualizar las plantillas de `legal/` y la ficha de seguridad de los datos (`PUBLICACION.md`).
12. **Los datos legales se escriben en un solo sitio** (`legal/datos-legales.properties`). La política se muestra **dentro de la app** (`PoliticaPrivacidadActivity` carga las páginas de `legal/` y las completa con esos datos), sin depender de la web; las páginas de `docs/` para la web se generan con `./gradlew generarPaginasLegales` y no se editan a mano.

## 10. Limitaciones y trabajo pendiente conocido

- **Flujos con cuenta real sin probar**: las pantallas posteriores al login se han recorrido en el emulador (Android 15, debug y release con R8) entrando sin sesión, con giros de pantalla y cierre del proceso incluidos. Lo que necesita una cuenta de Firebase —crear cuenta, iniciar sesión, cambiar la contraseña, **eliminar la cuenta**, la sesión caducada desde otro dispositivo y la llegada de los informes de Crashlytics a la consola— no se ha probado, y tampoco nada en un dispositivo con Android 16. La lista está en `PUBLICACION.md`.
- **Datos solo locales por dispositivo** (por UID). Sin sincronización en la nube; si se cambia de móvil no se recuperan.
- **Recaudación** usa los **precios vigentes** de la tirada (no se "congela" al cerrar).
- **Punto 16 pendiente**: registrar las tiradas de PlatoScore por series (hoy solo el total de platos por tirador). De ahí depende la **fase 2 del desempate** (automático por mejor última serie).
- **Tiradas de PlatoScore creadas antes de las cuentas** quedaron huérfanas (`userId=''`) y no las ve ningún usuario.
- `ordenDesempate` es único por tirador (caso límite si empata con gente distinta en dos categorías a la vez).
- Solo tests unitarios (no instrumentados). Hay un emulador (`Medium_Phone_API_35`, Android 15) que se puede arrancar en frío sin ventana (`-no-window -no-snapshot -gpu swiftshader_indirect`) para pruebas de humo.

## 11. Posibles direcciones

- **Migrar a Firestore** para sincronización multi-dispositivo.
- Implementar el **registro por series en PlatoScore** (punto 16) → habilita desempate automático.
- **Exportar a PDF** (hoy solo texto), estadísticas cruzadas (máquina×puesto), presets de fecha, etc.
- Antes de publicar en Play quedan pasos manuales (datos legales, alojar las páginas, clave de firma, ficha de seguridad de los datos): ver `PUBLICACION.md`.

---

*Historial completo en `git log --oneline` (un commit por funcionalidad con su justificación).*
