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

echo "PRUEBA COMPLETADA: APK instalada, app visible y proceso funcionando."
