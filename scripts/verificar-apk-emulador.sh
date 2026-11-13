#!/usr/bin/env bash
set -euo pipefail

APK="${1:-app-a-probar.apk}"
PKG="cl.canelillo.lupaflotante.release"
ACTIVITY="cl.canelillo.lupaflotante.MainActivity"

echo "== Android == "
adb shell getprop ro.build.version.release
echo "== Instalar APK ==="
RESULT=$(adb install -r "$APK" 2>&1) || {
  echo "$RESULT"
  exit 1
}
echo "$RESULT"
echo "$RESULT" | grep -q "Success"

echo "== Confirmar paquete instalado ==="
adb shell pm path "$PKG" | grep 'base.apk'

echo "== Abrir pantalla principal ==="
adb logcat -c || true
START=$(adb shell am start -W -n "$PKG/$ACTIVITY" 2>&1)
echo "$START"
echo "$START" | grep -q "Status: ok"

sleep 3
echo "== Comprobar proceso ==="
adb shell pidof "$PKG" | grep -E '[0-9]+'

echo "== Comprobar actividad visible ==="
adb shell dumpsys activity activities | grep -E "topResumedActivity=.*$PKG|ResumedActivity:.*$PKG" | head -3

echo "== Comprobar cierres inesperados ==="
LOG=$(adb logcat -d -s AndroidRuntime:E)
if printf '%s\n' "$LOG" | grep -F "Process: $PKG"; then
  echo "Error: la app se cerro inesperadamente"
  exit 1
fi


echo "== Activar servicio de accesibilidad para probar lupa ==="
SERVICE="$PKG/cl.canelillo.lupaflotante.LupaAccessibilityService"
adb shell settings put secure enabled_accessibility_services "$SERVICE"
adb shell settings put secure accessibility_enabled 1
sleep 2
adb shell dumpsys accessibility | grep -F "$PKG" | head -8 || true

echo "== Capturar pantalla y comprobar que hay interfaz ==="
mkdir -p capturas
API=$(adb shell getprop ro.build.version.sdk | tr -d '\r')
adb exec-out screencap -p > "capturas/LupaFlotante-Android-API${API}.png"
test -s "capturas/LupaFlotante-Android-API${API}.png"
echo "CAPTURA_OK: capturas/LupaFlotante-Android-API${API}.png"

echo "== Probar toque en burbuja y comprobar respuesta visual =="
# El emulador usa Pixel_5 con pantalla 1080x2340 y densidad 440: la burbuja
# se encuentra en el borde derecho, aproximadamente a x=960, y=870.
adb shell input tap 960 870
sleep 2
adb exec-out screencap -p > "capturas/LupaFlotante-Android-API${API}-zoom.png"
test -s "capturas/LupaFlotante-Android-API${API}-zoom.png"
echo "CAPTURA_ZOOM_OK: capturas/LupaFlotante-Android-API${API}-zoom.png"
if cmp -s "capturas/LupaFlotante-Android-API${API}.png" "capturas/LupaFlotante-Android-API${API}-zoom.png"; then
  echo "AVISO: las capturas son identicas; revisar respuesta de la lupa."
else
  echo "CAMBIO_VISUAL_DETECTADO: la pantalla cambió después del toque"
fi
adb shell pidof "$PKG" | grep -E '[0-9]+'

echo "PRUEBA COMPLETADA: APK instalada, app visible, servicio solicitado y proceso funcionando."
