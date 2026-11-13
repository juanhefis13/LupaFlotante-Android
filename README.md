# Lupa Flotante — Android

Aplicación de ampliación para Android 8 y posteriores, con una burbuja de accesibilidad que aparece sobre otras aplicaciones.

## Cómo funciona
- Un toque en la burbuja activa o desactiva la ampliación.
- Arrastrar mueve la burbuja y el centro de ampliación.
- Doble toque alterna entre 2×, 3× y 4×.
- Si el dispositivo admite la ampliación de ventana, la usa; de lo contrario intenta ampliación de pantalla completa.
- No captura pantalla, no lee el contenido de otras aplicaciones, no usa red.

## Obtener APK
Abre **Actions > Compilar APK de Lupa Flotante** y ejecuta **Run workflow** (o espera una ejecución automática por push). Al completarse, descarga el artefacto **LupaFlotante-APK-v1.1** y descomprímelo para encontrar `LupaFlotante-v1.1.apk`.

## Instalar
Instala el APK en Android 8+, abre la aplicación y habilita **Lupa Flotante** en los ajustes de Accesibilidad. Algunos Android 13+ requieren habilitar «Permitir configuración restringida» desde Información de la aplicación antes de activar un servicio de accesibilidad instalado por APK. Actívalo únicamente si confías en la aplicación.

**Prototipo:** el control depende del soporte de cada fabricante y se debe probar en el dispositivo real. El APK de debug usa firma de prueba, no es una versión de distribución.

## Si aparece «No se instaló la app»

El APK debe descargarse completo como archivo **.apk**, no como ZIP. Antes de instalar, revisa si existe otra versión de Lupa Flotante instalada: una versión firmada con otra clave de depuración no puede actualizarse encima de ella. Desinstalar la versión previa permite instalar desde cero, pero elimina la configuración guardada de esa app. Comprueba también que el administrador de archivos o navegador tenga permiso para instalar aplicaciones desde esa fuente y que el sistema no bloquee el APK mediante políticas de seguridad.

Para diagnosticar un error real de instalación con ADB conectado al teléfono: `adb install -r LupaFlotante-v1.1.apk`. La salida muestra el motivo (por ejemplo, `INSTALL_FAILED_UPDATE_INCOMPATIBLE` o `INSTALL_FAILED_VERSION_DOWNGRADE`). No hay que desactivar protección del sistema si el APK no es de confianza.

En CI se verifica que el APK tenga firma válida y que incluya el icono y las versiones SDK declaradas. **Una compilación correcta no equivale a una prueba de instalación en todos los teléfonos.**
