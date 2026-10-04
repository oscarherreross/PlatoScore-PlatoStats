# Publicar PlatoScore y PlatoStats en Google Play

Lo que el proyecto ya trae preparado y los pasos manuales que quedan. Todo vale para las dos apps salvo que se diga lo contrario.

## 1. Datos legales y páginas legales

Google Play exige una política de privacidad accesible desde la app y con dirección web y, para las apps con cuentas, una página web donde pedir la eliminación de la cuenta. Las cuatro páginas (dos por app) están redactadas como plantillas en `legal/`.

**Dentro de las apps no hay que hacer nada más.** La carpeta `legal/` va incluida en cada app, y el enlace «Política de privacidad» (en el inicio de sesión y en el menú lateral) abre la política en una pantalla propia, sin conexión y sin depender de ninguna web. Desde ella se llega a la página de eliminación de cuenta.

Lo que queda es rellenar tus datos y publicar las páginas en la web, que es lo que pide Play Console:

1. Rellena `legal/datos-legales.properties`: responsable y correo de contacto. Es el único sitio donde se escriben; mientras falten, las páginas de la app muestran «(pendiente de indicar)».
2. Genera las páginas para la web:
   ```bash
   ./gradlew generarPaginasLegales
   ```
   Aparecen en `docs/`, con tus datos ya puestos.
3. Publica la carpeta `docs/`. Con GitHub Pages: *Settings → Pages → Deploy from a branch → `main` / `docs`* (en un repositorio privado hace falta un plan de pago; sirve cualquier otro alojamiento de páginas estáticas, como Firebase Hosting).
4. Comprueba que estas direcciones abren, donde `<web>` es la dirección en la que has publicado `docs/`:

   | | PlatoScore | PlatoStats |
   |---|---|---|
   | Política de privacidad | `<web>/platoscore/privacidad.html` | `<web>/platostats/privacidad.html` |
   | Eliminación de cuenta | `<web>/platoscore/eliminar-cuenta.html` | `<web>/platostats/eliminar-cuenta.html` |

Para cambiar un texto, edita la plantilla en `legal/`: la app lo recoge al compilar, y para la web hay que volver a generar (lo que hay en `docs/` se sobrescribe).

> Los textos son un borrador redactado a partir de lo que hace el código. Revísalos antes de publicarlos: no sustituyen al asesoramiento legal. Si la app empieza a tratar más datos (sincronización en la nube, analítica…), hay que actualizarlos junto con la ficha del apartado siguiente.

## 2. Play Console

**Contenido de la aplicación → Política de privacidad:** la dirección de la política de cada app.

**Contenido de la aplicación → Seguridad de los datos.** Respuestas que corresponden al código actual:

| Pregunta | Respuesta |
|---|---|
| ¿La app recopila o comparte datos de usuario? | Sí |
| ¿Se cifran en tránsito todos los datos recopilados? | Sí (Firebase usa HTTPS) |
| Método de creación de cuentas | Nombre de usuario y contraseña |
| URL para solicitar la eliminación de la cuenta | La de «Eliminación de cuenta» de la tabla anterior |
| ¿Se puede pedir el borrado de datos sin eliminar la cuenta? | No |

Tipos de datos que hay que declarar. Los dos primeros los trata Firebase Authentication; los otros tres, Firebase Crashlytics (informes de errores):

| Tipo | Recopilado | Compartido | Obligatorio | Finalidad |
|---|---|---|---|---|
| Información personal → Dirección de correo electrónico | Sí | No | Sí | Gestión de cuentas |
| Información personal → IDs de usuario | Sí | No | Sí | Gestión de cuentas |
| Información y rendimiento de la app → Registros de fallos | Sí | No | No: el usuario puede desactivarlo | Análisis |
| Información y rendimiento de la app → Diagnóstico | Sí | No | No: el usuario puede desactivarlo | Análisis |
| ID de dispositivo u otros ID | Sí | No | No: el usuario puede desactivarlo | Análisis |

Lo que **no** se declara, y por qué:

- Tiradas, escuadras y tiradores (con su DNI y licencia) en PlatoScore, y tiradas y series en PlatoStats: se guardan solo en el dispositivo. Play llama «recopilar» a enviar datos fuera del dispositivo.
- La copia de seguridad de Android: la hace el sistema en la cuenta de Google del propio usuario, y el desarrollador no la recibe.

Contrasta la tabla con la guía de Firebase sobre este formulario (<https://firebase.google.com/docs/android/play-data-disclosure>) por si ha cambiado lo que recoge el SDK.

## 3. Firma

Lo habitual es **Play App Signing**: Google guarda la clave con la que firma lo que reciben los usuarios, y tú firmas lo que subes con una *clave de subida*. Si la pierdes, Google puede sustituirla; por eso es la opción recomendada.

1. Crea el almacén con la clave de subida, una sola vez (sirve para las dos apps):
   ```bash
   keytool -genkeypair -v -keystore plato-subida.jks -alias subida -keyalg RSA -keysize 2048 -validity 10000
   ```
   Guárdalo fuera del repositorio y haz copia de seguridad, también de las contraseñas.
2. Copia `keystore.properties.example` como `keystore.properties` en la raíz del proyecto y pon la ruta, el alias y las contraseñas. Ese archivo está en el `.gitignore` y no debe subirse nunca.

Sin `keystore.properties`, la variante release se compila igualmente pero sin firmar: sirve para comprobar que R8 no rompe nada, no para instalar ni publicar.

## 4. Versiones

Cada app lleva su propia versión, al principio de su `build.gradle.kts`:

```kotlin
val versionMayor = 1
val versionMenor = 0
val versionParche = 0
```

- El nombre visible es `mayor.menor.parche` (1.0.0).
- El código interno se calcula solo: `mayor × 10000 + menor × 100 + parche` (10000 para la 1.0.0). Por eso `menor` y `parche` no pueden pasar de 99.
- Google Play rechaza un código ya usado, aunque esa subida no llegara a publicarse: **cada subida necesita subir al menos el parche**.
- Criterio: `parche` para correcciones, `menor` para funciones nuevas, `mayor` para cambios grandes. Las dos apps no tienen por qué ir a la par.

## 5. Generar el paquete

```bash
./gradlew :app:bundleRelease
```

```bash
./gradlew :platostats:bundleRelease
```

El paquete queda en `app/build/outputs/bundle/release/` (y en `platostats/...`). Falla a propósito si los datos legales siguen sin rellenar. El archivo que traduce las trazas de error ofuscadas (`mapping.txt`) va dentro del paquete y Play lo usa solo; una copia queda en `build/outputs/mapping/release/`.

Al compilar una release **firmada** (con `keystore.properties`), ese mismo archivo se sube también a Firebase, para que Crashlytics muestre las trazas legibles. Hace falta conexión y el `google-services.json` real. Las compilaciones sin firmar no suben nada.

Para probar en un dispositivo la versión que se va a publicar, con R8 y firmada:

```bash
./gradlew :app:assembleRelease
```

## 6. Qué probar antes de subir

Comprobado en el emulador (Android 15), en debug y en release con R8: giros de pantalla con cada diálogo y cada formulario abiertos, filtros, restauración tras cerrarse el proceso, y arranque hasta el inicio de sesión.

**Informes de errores.** Crashlytics solo está activo en la versión release; en debug no envía nada. El usuario puede desactivarlo con el interruptor «Enviar informes de errores» del menú lateral. El panel aparece en la consola de Firebase (*Crashlytics*) cuando llega el primer informe.

Pendiente de probar a mano, porque necesita una cuenta real o un dispositivo que no había:

- [ ] Crear cuenta, iniciar sesión y recuperar la contraseña.
- [ ] Cambiar la contraseña desde el menú lateral.
- [ ] **Eliminar la cuenta**: que desaparece de Firebase (consola → Authentication), que se borran sus tiradas del dispositivo y que la app vuelve al inicio de sesión.
- [ ] **Sesión caducada**: con la misma cuenta abierta en dos dispositivos, eliminarla (o cambiar su contraseña) en uno y abrir la app en el otro: debe avisar y volver al inicio de sesión.
- [ ] **Crashlytics**: provocar un fallo en una versión release firmada y comprobar que el informe llega a la consola de Firebase con la traza legible.
- [ ] Un recorrido completo en un dispositivo con Android 16, que es donde `targetSdk 36` cambia cosas: el gesto de atrás predictivo y, en tabletas, que la app ya no puede fijar la orientación.
- [ ] La versión release firmada con la clave de subida, instalada desde una prueba interna de Play.
