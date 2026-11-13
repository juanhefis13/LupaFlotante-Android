# Lupa Flotante — Android

Aplicación nativa para controlar la ampliación del sistema Android mediante un botón flotante de accesibilidad, sin tomar capturas ni leer contenido de otras apps.

## Instalación: APK release oficial del proyecto

**Archivo que debes descargar:** [LupaFlotante-Release-v1.2.apk](https://github.com/juanhefis13/LupaFlotante-Android/releases/download/v1.2/LupaFlotante-Release-v1.2.apk)

- Descargar desde un navegador normal, como Chrome, sin descomprimir ni renombrar el archivo.
- Instalar el archivo `.apk` desde Descargas.
- Si aparece una ventana de autorización, habilitar instalación desde esa fuente **solo si confías en la APK**.
- Abrir Lupa Flotante > Activar en Accesibilidad para mostrar la burbuja.
- En Android 13+ el sistema puede pedir habilitar la configuración restringida de accesibilidad para aplicaciones instaladas fuera de la tienda.

La app tiene un icono azul con una lupa blanca. Compatibilidad declarada: Android 8 y superior.

## Controles

- Toque en la burbuja: activar/desactivar ampliación.
- Arrastrar: mover la burbuja y el centro del zoom.
- Doble toque: alternar entre aumentos predefinidos.
- Ajustar el aumento desde la pantalla principal.

## Verificación técnica y limitaciones

- Compilación `assembleRelease`, firma APK v2 y verificación de metadatos ejecutadas.
- La APK release publicada de v1.2 tiene SHA-256 `99570eca886d5da1025d9fcaae7ecfac500e1984e82c6bdf58897ceb4c4ad406`.
- Las pruebas en emulador están en [Actions](https://github.com/juanhefis13/LupaFlotante-Android/actions/workflows/diagnosticar-instalacion.yml).
- **Importante**: instalación y apertura en un emulador no garantizan instalación en todos los fabricantes y modelos; el sistema del teléfono puede bloquear APKs externas.
- Una app nativa pequeña puede pesar unos KB si no lleva bibliotecas externas ni recursos multimedia.

## Si Android indica «No se instaló la app»

No asumir que el problema es la versión de Android. Revisar, según el teléfono:

1. Confirmar que el archivo de Descargas es `LupaFlotante-Release-v1.2.apk`, no un ZIP ni un archivo HTML.
2. Confirmar que el sistema permite instalar APKs desde el navegador o gestor de archivos elegido.
3. Verificar la presencia de otra aplicación instalada con el **mismo identificador** y firma diferente. La release v1.2 usa el identificador `cl.canelillo.lupaflotante.release`, diferente de los APK de depuración anteriores.
4. Revisar mensajes del sistema de seguridad del fabricante. Evitar desactivar protecciones de seguridad sin entender el bloqueo.
5. Para obtener la causa exacta, con teléfono y ADB autorizados: `adb install -r LupaFlotante-Release-v1.2.apk`. Ese comando informa el código de fallo real.

## Para desarrolladores

- Código: Java Android nativo, minSdk 26.
- Compilación release en GitHub Actions: manual.
- **La firma de la release v1.2 es temporal y no se almacena en este repositorio público.** Para permitir actualizaciones instalables sin desinstalar, configurar en GitHub Actions una clave permanente protegida por secretos antes de publicar una nueva release.
- No se transmite información ni se requiere Internet para la aplicación.

