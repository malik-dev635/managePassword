@echo off
REM Initialisation Git + push sur GitHub master

echo.
echo 🚀 Initialisation du dépôt Git...

git init

echo # Fichiers et dossiers à ignorer > .gitignore

echo *.jar >> .gitignore

echo ✅ .gitignore généré.

echo #Projet gestionnaire de mots de passe JavaFX > README.md
echo Ce projet est une interface JavaFX simple avec une connexion et inscription et une interface simple pour gerer les mots de passe. >> README.md

git add .

git commit -m "🎉 Premier commit - initialisation du projet"

REM Ajout remote GitHub
git remote add origin https://github.com/malik-dev635/managePassword.git

REM Pousse sur master
git push -u origin master

echo ✅ Dépôt Git initialisé et poussé sur GitHub (branche master).
pause
