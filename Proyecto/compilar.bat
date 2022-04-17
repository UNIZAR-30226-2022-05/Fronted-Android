@echo off

IF not exist gradlew.bat (
    echo Error: El archivo "gradlew.bat" no existe
    echo Has colocado "compilar.bat" en el directorio del proyecto?
    pause
    exit 1
)

title Compilando unoforall.apk...
del /Q unoforall.apk >nul 2>&1

call gradlew.bat assembleDebug
if NOT "%ERRORLEVEL%" == "0" (
    title Error en la compilacion
    pause
    exit /B 1
)

copy ".\app\build\outputs\apk\debug\app-debug.apk" .\unoforall.apk

title Compilacion completada

pause

rem Para limpiar los archivos de compilación, usar: gradlew.bat clean

rem El archivo unoforall.apk aparecerá en la carpeta actual
