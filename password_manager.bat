@echo off
setlocal

:: ➤ Chemin vers JavaFX SDK (modifie si nécessaire)
set JAVAFX_SDK=C:\javafx-sdk-21.0.7

:: ➤ Dossiers du projet
set SRC=src
set OUT=out

:: ➤ Nettoyage de l'ancien dossier de sortie
if exist %OUT% (
    rmdir /s /q %OUT%
)
mkdir %OUT%

:: ➤ Compilation
echo [Compilation en cours...]
javac --module-path "%JAVAFX_SDK%\lib" ^
      --add-modules javafx.controls,javafx.fxml ^
      -d %OUT% ^
      %SRC%\module-info.java ^
      %SRC%\passwordmanager\*.java

if %errorlevel% neq 0 (
    echo Échec de la compilation. Vérifie les erreurs ci-dessus.
    pause
    exit /b
)

:: ➤ Exécution
echo [Lancement de l'application...]
java --module-path "%JAVAFX_SDK%\lib;%OUT%" ^
     --add-modules javafx.controls,javafx.fxml ^
     -m passwordmanager/passwordmanager.Main

pause
