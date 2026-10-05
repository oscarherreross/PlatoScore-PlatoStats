# Publicar una versión de PlatoScore y PlatoStats

Las apps **no se publican en Google Play**. El código está en este repositorio y cada versión firmada se adjunta como APK en [GitHub Releases](https://github.com/oscarherreross/PlatoScore-PlatoStats/releases), para quien quiera instalarla. Todo vale para las dos apps salvo que se diga lo contrario.

El [anexo](#anexo-si-algún-día-se-publican-en-google-play) conserva lo que haría falta para Google Play.

## 1. Datos legales

Las apps crean cuentas (correo y contraseña en Firebase), así que quien reparte un APK responde de esos datos. La política de privacidad y la página de eliminación de cuenta de cada app están redactadas como plantillas en `legal/` y **van dentro de las apps**: el enlace «Política de privacidad» (en el inicio de sesión y en el menú lateral) las abre sin conexión.

Antes de repartir un APK, rellena `legal/datos-legales.properties` con el responsable y un correo de contacto. Es el único sitio donde se escriben; mientras falten, la política muestra «(pendiente de indicar)» y la compilación de release avisa.

Para cambiar un texto, edita la plantilla en `legal/`: la app lo recoge al compilar.

> Los textos son un borrador redactado a partir de lo que hace el código. Revísalos antes de repartir la app: no sustituyen al asesoramiento legal. Si la app empieza a tratar más datos (sincronización en la nube, analítica…), hay que actualizarlos.

## 2. Firma

Android solo instala APK firmados, y solo actualiza una app si la versión nueva está firmada con **la misma clave** que la instalada. Fuera de Google Play nadie guarda esa clave por ti: si se pierde, quien tenga la app tendrá que desinstalarla (y perder sus datos) para instalar una versión firmada con otra.

1. Crea el almacén con la clave, una sola vez (sirve para las dos apps):
   ```bash
   keytool -genkeypair -v -keystore plato-firma.jks -alias plato -keyalg RSA -keysize 2048 -validity 10000
   ```
   Guárdalo **fuera del repositorio** y haz copia de seguridad, también de las contraseñas.
2. Copia `keystore.properties.example` como `keystore.properties` en la raíz del proyecto y pon la ruta, el alias y las contraseñas. Ese archivo está en el `.gitignore` y no debe subirse nunca.

Sin `keystore.properties`, la variante release se compila igualmente pero sin firmar: sirve para comprobar que R8 no rompe nada, no para instalar.

## 3. Versiones

Cada app lleva su propia versión, al principio de su `build.gradle.kts`:

```kotlin
val versionMayor = 1
val versionMenor = 0
val versionParche = 0
```

- El nombre visible es `mayor.menor.parche` (1.0.0).
- El código interno se calcula solo: `mayor × 10000 + menor × 100 + parche` (10000 para la 1.0.0). Por eso `menor` y `parche` no pueden pasar de 99.
- Android no instala encima de una app una versión con un código menor: **cada versión que se reparte sube al menos el parche**.
- Criterio: `parche` para correcciones, `menor` para funciones nuevas, `mayor` para cambios grandes. Las dos apps no tienen por qué ir a la par.

## 4. Generar y publicar una versión

1. Comprueba que `main` está en verde en la integración continua y que los `google-services.json` reales están en `app/` y `platostats/`.
2. Genera los dos APK firmados (con R8):
   ```bash
   ./gradlew :app:assembleRelease :platostats:assembleRelease
   ```
   Quedan en `app/build/outputs/apk/release/app-release.apk` y `platostats/build/outputs/apk/release/platostats-release.apk`.
3. Instálalos en un dispositivo y pruébalos: es la única compilación en la que Crashlytics envía informes.
4. En GitHub, *Releases → Draft a new release*: crea una etiqueta (`v1.0.0`), describe los cambios y adjunta los dos APK con nombres claros (`PlatoScore-1.0.0.apk`, `PlatoStats-1.0.0.apk`).

Los APK **no se guardan en el repositorio** (están en el `.gitignore`); solo se adjuntan a la versión.

Al compilar una release firmada, el archivo que traduce las trazas de error ofuscadas (`mapping.txt`) se sube a Firebase para que Crashlytics muestre trazas legibles. Hace falta conexión. Las compilaciones sin firmar no suben nada. Una copia queda en `build/outputs/mapping/release/`.

Quien instale el APK tiene que permitir en su dispositivo la instalación desde el navegador o el gestor de archivos, y verá el aviso habitual de Play Protect para apps de fuera de la tienda.

**Firebase.** Un APK lleva dentro la clave de API de su proyecto de Firebase, como cualquier app Android; no es un secreto, pero conviene acotarla. En Google Cloud (*API y servicios → Credenciales*), restringe la clave de cada proyecto a su paquete (`oscar.platoscore` u `oscar.platostats`) y a la huella SHA-1 de la clave de firma, y deja solo las API que usa la app.

## 5. Qué queda por probar

Comprobado en el emulador (Android 15), en debug y en release con R8: giros de pantalla con cada diálogo y cada formulario abiertos, filtros, restauración tras cerrarse el proceso, el arranque hasta el inicio de sesión, la política de privacidad y, entrando sin sesión, las pantallas de las dos apps (tiradas, escuadras, tiradores, clasificaciones, series y estadísticas).

**Informes de errores.** Crashlytics solo está activo en la variante release; en debug no envía nada. El usuario puede desactivarlo con el interruptor «Enviar informes de errores» del menú lateral. El panel aparece en la consola de Firebase (*Crashlytics*) cuando llega el primer informe.

Pendiente de probar a mano, porque necesita una cuenta real o un dispositivo que no había:

- [ ] Crear cuenta, iniciar sesión y recuperar la contraseña.
- [ ] Cambiar la contraseña desde el menú lateral.
- [ ] **Eliminar la cuenta**: que desaparece de Firebase (consola → Authentication), que se borran sus tiradas del dispositivo y que la app vuelve al inicio de sesión.
- [ ] **Sesión caducada**: con la misma cuenta abierta en dos dispositivos, eliminarla (o cambiar su contraseña) en uno y abrir la app en el otro: debe avisar y volver al inicio de sesión.
- [ ] **Crashlytics**: provocar un fallo en una versión release firmada y comprobar que el informe llega a la consola de Firebase con la traza legible.
- [ ] Un recorrido completo en un dispositivo con Android 16, que es donde `targetSdk 36` cambia cosas: el gesto de atrás predictivo y, en tabletas, que la app ya no puede fijar la orientación.

## Anexo: si algún día se publican en Google Play

El proyecto quedó preparado para ello (targetSdk 36, R8, eliminación de cuenta desde la app, política de privacidad). Lo que faltaría:

- **Cuenta de desarrollador.** Una cuenta personal muestra en la ficha el nombre legal del titular y exige, antes de publicar, una prueba cerrada de cada app con 12 testers durante 14 días seguidos.
- **Páginas legales en la web.** Play pide una dirección web para la política de privacidad y otra para solicitar la eliminación de la cuenta. Con los datos legales rellenos, `./gradlew generarPaginasLegales` las genera en `docs/`, listas para GitHub Pages (*Settings → Pages → `main` / `docs`*).
- **Paquete.** `./gradlew :app:bundleRelease` (y `:platostats:bundleRelease`) genera el `.aab`; falla a propósito si faltan los datos legales. Con Play App Signing, la clave del apartado 2 pasa a ser la clave de subida.
- **Seguridad de los datos.** Respuestas que corresponden al código actual: la app recopila datos, cifrados en tránsito; las cuentas se crean con usuario y contraseña; no se puede pedir el borrado de datos sin eliminar la cuenta. Tipos que hay que declarar:

  | Tipo | Obligatorio | Finalidad |
  |---|---|---|
  | Dirección de correo electrónico | Sí | Gestión de cuentas |
  | IDs de usuario | Sí | Gestión de cuentas |
  | Registros de fallos | No: el usuario puede desactivarlo | Análisis |
  | Diagnóstico | No: el usuario puede desactivarlo | Análisis |
  | ID de dispositivo u otros ID | No: el usuario puede desactivarlo | Análisis |

  Ninguno se comparte con terceros. No se declaran las tiradas, escuadras, tiradores ni series (se guardan solo en el dispositivo) ni la copia de seguridad de Android (la hace el sistema en la cuenta del usuario). Contrasta la tabla con la [guía de Firebase](https://firebase.google.com/docs/android/play-data-disclosure) por si ha cambiado lo que recoge el SDK.
