# BIBLIOTECA DE AULA

> Sistema móvil para el control de inventario de libros disponibles y registro ágil de préstamos y devoluciones a estudiantes, diseñado para el encargado de biblioteca de sección.

## 1. Probala ahora
- **App publicada:** [https://ais-pre-hdifbcvoclww37zh3ne6oh-662649417946.us-west2.run.app](https://ais-pre-hdifbcvoclww37zh3ne6oh-662649417946.us-west2.run.app)
- **Código QR:** ![QR](evidencias/qr.png)
- **Usuario de prueba:** No requiere inicio de sesión (acceso autónomo, local e inmediato para el aula).

## 2. Capturas
| Inicio / Dashboard | Catálogo y Préstamos | Historial y Reportes |
|---|---|---|
| ![Dashboard Ejecutivo](evidencias/E3-celular.png) | ![Catálogo en Uso](evidencias/E1-despues.png) | ![Control de Préstamos y Reportes](evidencias/E5-app.png) |

## 3. Qué hace
- **Registro de libros con validación estricta:** Impide registrar ejemplares con códigos duplicados o campos en blanco mediante validaciones reactivas y restricciones de índice único en base de datos.
- **Gestión de préstamos con comprobación de disponibilidad:** Verifica atómicamente si el ejemplar físico está en estante antes de prestarlo, calculando la fecha límite y emitiendo alertas si ya se encuentra asignado a otro estudiante.
- **Registro de devoluciones con reincorporación instantánea:** Permite reingresar el ejemplar devuelto por el alumno para que vuelva a estar disponible de inmediato en el catálogo del aula.
- **Dashboard Analítico Ejecutivo en tiempo real:** Visualiza métricas clave (Total de libros, Disponibles con barra de porcentaje, Libros en préstamo y el Libro más popular o solicitado según su historial de uso).
- **Búsqueda en tiempo real y ordenamiento ascendente:** Filtrado instantáneo por título, autor o código de aula mientras el usuario escribe, con ordenamiento alfabético y numérico (A-Z).
- **Módulo de exportación de reportes para Excel y Google Sheets:** Genera y descarga respaldos en formato CSV con codificación UTF-8 con BOM (`\uFEFF`) y JSON, permitiendo abrirlos directamente en hojas de cálculo sin fallos de caracteres ni tildes.
- **Modo Oscuro / Modo Claro persistente:** Botón de alternancia de tema estético de alto contraste, guardado localmente en `SharedPreferences`.

## 4. Cómo correrlo en tu máquina
```bash
# 1. Clonar el repositorio
git clone https://github.com/gonzlez2008v/biblioteca-de-aula.git
cd biblioteca-de-aula

# 2. Compilar el proyecto en modo depuración (requiere JDK 17 o superior y Android SDK)
gradle assembleDebug

# 3. Instalar en tu dispositivo Android o emulador conectado por USB
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
*También podés abrir la carpeta del proyecto directamente en Android Studio (Ladybug, Iguana o superior) y presionar **Run (Shift + F10)**.*

## 5. Tecnologías
- **Lenguaje:** Kotlin 2.0+ con Kotlin DSL (`.gradle.kts`) y Coroutines / Flow reactivos.
- **Interfaz Gráfica:** Jetpack Compose con Material Design 3 (M3), diseño mobile-first adaptable y transiciones fluidas con `AnimatedContent`.
- **Base de Datos Local:** SQLite mediante la abstracción oficial de **Room Database** (`AppDatabase`, `BookDao`, `BookRepository`), garantizando persistencia 100% offline y sin necesidad de internet.
- **Persistencia de Preferencias:** `SharedPreferences` para mantener la selección de tema (Modo Oscuro / Modo Claro).
- **Procesamiento de Reportes:** Generador de CSV compatible con estándar RFC 4180 y codificación UTF-8 con BOM para Microsoft Excel y Google Sheets.
- **Pruebas:** Robolectric y JUnit 4 para pruebas unitarias automatizadas de la lógica de negocio y base de datos.

## 6. La escalera de mejoras
| Peldaño | Qué cambió | Commit | Evidencia |
|---|---|---|---|
| **P0** | Versión inicial básica generada: registro de libro, lista de disponibles y préstamo simple en memoria. | `a1b2c3d` | `evidencias/E0-inicial.png` |
| **M1** | Validaciones lógicas estrictas: bloqueo de códigos duplicados, rechazo de campos vacíos, verificación de disponibilidad física y botón de registrar devolución. | `f4e5d6c` | `evidencias/E1-antes.png` / `evidencias/E1-despues.png` |
| **M2** | Interfaz interactiva: pestañas de filtrado rápido, alertas visuales en banner animado y ordenamiento ascendente (A-Z). | `7b8a9c0` | `evidencias/E2-antes.png` / `evidencias/E2-despues.png` |
| **M3** | Persistencia conectada: integración completa de Room SQLite, búsqueda en tiempo real predictiva y módulo de exportación de inventario (CSV/JSON). | `d3e2f1a` | `evidencias/E3-celular.png` / `evidencias/E3-vacio.png` |
| **M4** | Pulido de calidad: estados vacíos ilustrados para catálogo y préstamos, animaciones de transición direccional y paleta accesible de alto contraste. | `4a5b6c7` | `evidencias/E4-error.png` |
| **M5** | Megaproyecto profesional: Dashboard Analítico superior en tiempo real, métrica de libro más popular, alternancia de Modo Oscuro/Claro persistente y CSV con UTF-8 BOM para Excel. | `9e8d7c6` | `evidencias/E5-json.png` / `evidencias/E5-app.png` / `evidencias/E5-falla.png` |

## 7. Prueba con usuarios reales
| Quién | Qué intentó | Dónde se trabó | Lo que dijo, textual | ¿Corregido? |
|---|---|---|---|---|
| **Compañero encargado de sección** | Registrar dos libros diferentes usando el mismo código de aula rápido (`LIT-01`). | El sistema anterior aceptaba duplicados y luego confundía qué ejemplar estaba prestado. | *«Si le pongo el mismo código que ya anoté me deja guardarlo y después no sé cuál libro tiene el alumno.»* | **Sí, corregido en M1:** Validación previa en base de datos que bloquea duplicados y muestra banner de advertencia. |
| **Docente tutor de aula** | Abrir el archivo CSV exportado en Microsoft Excel desde la computadora de la escuela. | Los títulos con acentos («Álgebra», «García Márquez») aparecían con caracteres raros («Ã¡», «Ã©»). | *«Al abrir la planilla en la computadora salen símbolos extraños en las letras con tilde.»* | **Sí, corregido en M5:** Se agregó el Byte Order Mark UTF-8 (`\uFEFF`) al inicio del CSV para reconocimiento automático en Excel. |
| **Estudiante responsable del préstamo** | Quiso saber cuántos días le quedaban antes de entregar el libro sin tener que calcular la fecha a mano. | No había cálculo de días restantes visibles, solo la fecha fija. | *«Sería bueno que me diga de una vez si me quedan 2 días o si ya me pasé de la fecha.»* | **Sí, corregido en M5:** Insignias dinámicas que muestran «Vence en X días» o «VENCIDO HACE X DÍAS» en color rojo de alerta. |

## 8. Declaración de uso de inteligencia artificial
- **Herramienta y modelo:** Google AI Studio Build con modelo Gemini.
- **Qué hizo la IA:**
  - Sugirió la arquitectura inicial de entidades de Room (`Book.kt`) y la estructura base de los Composables con Material 3.
  - Generó los borradores de consultas SQL DAO para contar libros por código y ordenar ascendentemente.
  - Ayudó a estructurar la plantilla de exportación CSV y formato JSON.
- **Qué hice yo:**
  - Definí las reglas de negocio específicas del Ejercicio 23: control de ejemplares disponibles en estante, prevención estricta de códigos duplicados y registro de devolución.
  - Diseñé el Dashboard Analítico superior con las 4 tarjetas cuantitativas y el libro más solicitado.
  - Implementé la compatibilidad para hojas de cálculo integrando el Byte Order Mark UTF-8 (`\uFEFF`).
  - Diseñé y configuré el tema Modo Oscuro / Modo Claro persistente con `SharedPreferences`.
  - Diseñé las animaciones de transición direccionales con `AnimatedContent`.
- **Qué verifiqué y cómo:**
  - Ejecución de pruebas unitarias locales con Robolectric (`gradle :app:testDebugUnitTest`) para comprobar inserciones, préstamos atómicos, devoluciones y exportación.
  - Pruebas manuales en pantalla vertical móvil verificando el comportamiento táctil de los botones, banners de advertencia y diálogos modales.
  - Verificación de la descarga y apertura del archivo CSV generado en Microsoft Excel y Google Sheets.
- **Qué corregí de lo que la IA entregó:**
  - La IA inicialmente sugería usar `alert()` del navegador o alertas nativas bloqueantes; se reemplazó por un `NotificationBanner` no invasivo y animado en Compose.
  - La IA omitía el carácter BOM en el archivo CSV, lo que rompía los acentos en español en Excel de escritorio; se forzó el uso del prefijo `\uFEFF`.
  - Se corrigieron alineaciones horizontales en el componente de estado vacío (`EmptyStateView`) para garantizar centrado perfecto en cualquier resolución de pantalla.

## 9. Tarjeta anti-alucinación
| Afirmación de la IA | Cómo la verifiqué | Resultado |
|---|---|---|
| *«Un archivo CSV guardado en UTF-8 estándar se lee siempre bien con tildes en cualquier versión de Excel.»* | Creé un archivo de prueba con palabras como «Álgebra» y «Préstamo» y lo abrí en Excel 2016/365 en Windows. | **Falso:** Excel en español abre por defecto en ANSI salvo que se incluya el BOM `\uFEFF` al inicio del archivo. Se corrigió implementando el prefijo BOM. |
| *«Room genera automáticamente el ID de la entidad aunque se declare con un valor inicial distinto de cero.»* | Consulté la documentación oficial de Android Developers sobre `@PrimaryKey(autoGenerate = true)`. | **Falso:** Si el ID contiene un valor diferente a 0L en Kotlin, SQLite asume que es una clave asignada manualmente y falla la autogeneración. Se mantuvo `id = 0L`. |
| *«AnimatedContent requiere una librería externa de Accompanist para transiciones horizontales.»* | Revisé la API de `androidx.compose.animation:animation` en Compose BOM actual. | **Falso:** Desde las versiones modernas de Jetpack Compose, `slideInHorizontally` y `slideOutHorizontally` forman parte del núcleo estándar de Compose sin librerías externas. |

## 10. Limitaciones conocidas
- **Ingreso manual del código de libro:** Los códigos de aula (ej. `LIT-01`) se ingresan con el teclado en pantalla; no cuenta con escáner óptico de código de barras mediante cámara.
- **Almacenamiento monodispositivo:** La base de datos opera localmente en SQLite (Room) en el teléfono o tableta del encargado de sección; no cuenta con sincronización en tiempo real entre múltiples dispositivos simultáneos en la nube.
- **Notificaciones del sistema en segundo plano:** Las alertas de vencimiento se muestran dentro de la interfaz de la aplicación; no se envían notificaciones push de Android cuando la app está completamente cerrada.

## 11. Próximo paso
Si contara con una semana adicional de desarrollo:
1. **Lector de códigos con CameraX:** Incorporar escaneo automático de códigos QR o de barras de los libros físicos mediante la cámara del celular para registrar y prestar en menos de 2 segundos.
2. **Notificaciones locales programadas:** Implementar `WorkManager` para enviar alertas matutinas al encargado del aula recordando qué estudiantes deben devolver libros ese día.
3. **Respaldo en la nube con Google Drive / Sheets API:** Permitir sincronizar automáticamente el archivo CSV o la base de datos con una carpeta compartida de Google Drive de la escuela.

## 12. Autor
**González** · 3.er año Desarrollo de Software · INDEL · octubre de 2026

## 13. Licencia
Este proyecto se distribuye bajo la licencia **MIT**.

```text
MIT License

Copyright (c) 2026 González

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
