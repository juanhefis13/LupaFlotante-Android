# Lupa Flotante — Android

Aplicación de ampliación para Android 13 y posteriores, con una burbuja de accesibilidad que aparece sobre otras aplicaciones.

## Cómo funciona
- Un toque en la burbuja activa o desactiva la ampliación.
- Arrastrar mueve la burbuja y el centro de ampliación.
- Doble toque alterna entre 2×, 3× y 4×.
- Si el dispositivo admite la ampliación de ventana, la usa; de lo contrario intenta ampliación de pantalla completa.
- No captura pantalla, no lee el contenido de otras aplicaciones, no usa red.

## Obtener APK
Abre **Actions > Compilar APK de Lupa Flotante** y ejecuta **Run workflow** (o espera una ejecución automática por push). Al completarse, descarga el artefacto **LupaFlotante-APK** y descomprímelo para encontrar `app-debug.apk`.

## Instalar
Instala el APK en Android 13+, abre la aplicación y habilita **Lupa Flotante** en los ajustes de Accesibilidad. Algunos Android 13+ requieren habilitar «Permitir configuración restringida» desde Información de la aplicación antes de activar un servicio de accesibilidad instalado por APK. Actívalo únicamente si confías en la aplicación.

**Prototipo:** el control depende del soporte de cada fabricante y se debe probar en el dispositivo real. El APK de debug usa firma de prueba, no es una versión de distribución.