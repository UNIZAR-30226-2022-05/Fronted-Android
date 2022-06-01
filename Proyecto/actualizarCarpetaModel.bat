@echo off
set dir=%CD%
cd app\src\main\java\es\unizar\unoforall
set androidDir=%CD%
rmdir /S /Q model
cd %dir%
cd ..\..\Backend\Proyecto\src\es\unizar\unoforall
xcopy /E /I "%CD%\model" "%androidDir%\model"
pause
