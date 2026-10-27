# Examen de Desarrollo Móvil

Aplicación Android desarrollada con Kotlin y Jetpack Compose. El proyecto implementa dos ejercicios independientes que suman un total de **100 puntos**.

## Integrantes

- Angel Fernando Belmonte Escalante
- Melanny Estrella Sandoval Arnez
- Said Acosta Revollo

## Combinación elegida

| Ejercicio | Descripción | Puntaje |
|---|---|---:|
| E75-1 | Remote Config, Firestore, Room y notificación local | 75 puntos |
| E25-7 | WorkManager con restricciones y reintentos | 25 puntos |
| **Total** | | **100 puntos** |

Los ejercicios se encuentran en pantallas separadas y se pueden seleccionar desde el menú principal.

## Requisitos

- Android Studio con el SDK de Android requerido por el proyecto.
- JDK y Gradle compatibles con la configuración del proyecto.
- Un emulador o dispositivo Android con conexión a internet.
- Un proyecto de Firebase con una aplicación Android registrada.
- El proyecto de Design System publicado en Maven Local.

## Configuración del proyecto

### 1. Publicar el Design System en Maven Local

Abrir el proyecto del Design System y ejecutar:

```bash
./gradlew publishToMavenLocal --no-configuration-cache
```

En Windows también se puede ejecutar:

```powershell
.\gradlew.bat publishToMavenLocal --no-configuration-cache
```

Esta publicación permite que la aplicación encuentre la dependencia local del Design System.

### 2. Configurar Firebase

1. Crear o seleccionar un proyecto en [Firebase Console](https://console.firebase.google.com/).
2. Registrar una aplicación Android con el identificador:

   ```text
   org.ucb.appp1
   ```

3. Descargar el archivo `google-services.json` y colocarlo en:

   ```text
   androidApp/google-services.json
   ```

4. En **Firestore Database**, crear una base de datos si todavía no existe.
5. Para las pruebas, permitir la lectura y escritura del documento `configRemota/actual`. Se puede agregar esta regla dentro de las reglas existentes de la base de datos:

   ```javascript
   match /configRemota/actual {
     allow read, write: if true;
   }
   ```

   Esta regla es únicamente para pruebas: permite que cualquier cliente acceda a ese documento. Para una aplicación pública se deben configurar reglas apropiadas con autenticación.

6. En **Remote Config**, crear el parámetro:

   | Campo | Valor |
   |---|---|
   | Nombre | `monitoring` |
   | Tipo | Boolean |
   | Valor inicial | `false` |

7. Guardar y pulsar **Publicar cambios**.

El documento `configRemota/actual` se crea desde la aplicación durante la primera verificación si todavía no existe.

### 3. Ejecutar la aplicación

1. Abrir el proyecto principal en Android Studio.
2. Esperar a que termine la sincronización de Gradle.
3. Seleccionar la configuración de ejecución **androidApp**.
4. Seleccionar un emulador o dispositivo Android.
5. Ejecutar la aplicación.
6. En Android 13 o superior, aceptar el permiso de notificaciones cuando se solicite.

## Ejercicio 1: Remote Config, Firestore y notificación

### Funcionamiento

La aplicación obtiene el valor booleano `monitoring` desde Remote Config al pulsar **Verificar ahora**.

El valor se refleja en el documento de Firestore:

```text
configRemota/actual
```

La primera verificación guarda un valor inicial en Room. Cuando se detecta un cambio respecto al último valor guardado localmente, se añade un registro al historial y se genera una notificación local con el mensaje **Configuración actualizada**.

La pantalla también muestra la fecha de la última sincronización completada.

### Cómo probarlo

1. En el menú principal, entrar a **Ejercicio 1 · Monitoring**.
2. Pulsar **Verificar ahora**.
3. Comprobar que aparece el valor inicial en el historial.
4. En Firebase Remote Config, cambiar `monitoring` al valor contrario.
5. Guardar y pulsar **Publicar cambios**.
6. Volver a la app y pulsar **Verificar ahora**.
7. Comprobar que aparece el nuevo valor en el historial y que llega la notificación.
8. En Firestore, revisar que `configRemota/actual` contiene el mismo valor.
9. Pulsar nuevamente **Verificar ahora** sin cambiar el parámetro: la pantalla debe indicar que no hubo cambios.

## Ejercicio 2: WorkManager con reintentos

### Funcionamiento

La pantalla permite iniciar un trabajo único que simula una descarga durante dos segundos. Cuando termina correctamente, guarda la fecha y hora en `SharedPreferences`.

El trabajo requiere conexión a internet y utiliza una política de reintentos exponencial. La pantalla muestra su estado y permite cancelarlo.

### Cómo probar una descarga normal

1. En el menú principal, entrar a **Ejercicio 2 · WorkManager**.
2. Comprobar que el dispositivo tenga conexión.
3. Dejar desmarcada la opción **Simular fallo en el primer intento**.
4. Pulsar **Iniciar descarga**.
5. El trabajo debe finalizar en estado `SUCCEEDED` y mostrar la fecha de la última descarga.

### Cómo probar el reintento

1. Marcar **Simular fallo en el primer intento**.
2. Pulsar **Iniciar descarga**.
3. El trabajo vuelve a `ENQUEUED` después del primer intento simulado.
4. Esperar al menos 10 segundos; el tiempo real puede ser mayor.
5. El trabajo debe volver a ejecutarse y finalizar en `SUCCEEDED`.

### Cómo probar la cancelación

1. Desactivar la conexión a internet del dispositivo o emulador.
2. Pulsar **Iniciar descarga**.
3. El trabajo debe permanecer en `ENQUEUED` mientras espera la conexión.
4. Pulsar **Cancelar**.
5. Comprobar que el estado cambie a `CANCELLED`.
6. Reactivar la conexión al terminar la prueba.

## Decisiones técnicas

- **Jetpack Compose:** se utiliza para construir las pantallas y los botones de la aplicación.
- **Firebase Remote Config:** almacena el parámetro `monitoring`.
- **Cloud Firestore:** mantiene el valor actual en `configRemota/actual`.
- **Room y Flow:** guardan y muestran de forma reactiva el historial local de valores y fechas.
- **Notificación local:** avisa cuando la app detecta un cambio posterior al valor inicial.
- **SharedPreferences:** guarda la última sincronización y la fecha de la última descarga en archivos de preferencias separados.
- **WorkManager:** ejecuta el trabajo en segundo plano, administra la restricción de red, el reintento y la cancelación.
- **Reintento exponencial:** el primer tiempo de espera configurado es de 10 segundos.
- **Menú principal:** permite acceder a cada ejercicio desde su propia pantalla.

Para facilitar las pruebas del ejercicio, el intervalo mínimo de consulta de Remote Config se configuró en cero. Esta configuración es para desarrollo y puede estar sujeta a límites del servicio.
