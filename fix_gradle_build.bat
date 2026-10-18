@echo off
echo ================================
echo  FIX GRADLE BUILD (Auto Script)
echo ================================
echo.

:: Masuk ke folder project
cd /d C:\Users\rsspv\Documents\AndroidStudio\ESmansa

:: 1. Stop gradle daemon
echo [1/4] Hentikan Gradle Daemon...
gradlew --stop

:: 2. Kill semua proses java (kadang masih ngunci file)
echo [2/4] Kill proses java.exe kalau ada...
taskkill /F /IM java.exe >nul 2>&1

:: 3. Hapus cache gradle yang rusak
echo [3/4] Hapus cache Gradle...
rmdir /S /Q "C:\Users\rsspv\.gradle\caches" 

:: 4. Build ulang project
echo [4/4] Build ulang APK (assembleDebug)...
gradlew clean --refresh-dependencies
gradlew assembleDebug --stacktrace

echo.
echo =================================
echo  SELESAI! Cek hasil di folder app/build/outputs/apk/debug
echo =================================
pause
