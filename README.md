# Projet Gestionnaire de Mots de Passe JavaFX

Ce projet est une application JavaFX simple conçue pour aider les utilisateurs à gérer leurs mots de passe en toute sécurité. Il dispose d'une interface conviviale avec des fonctionnalités de connexion et d'inscription, permettant aux utilisateurs de stocker et de récupérer leurs identifiants efficacement.

## Fonctionnalités

- **Inscription et Connexion Utilisateur**: Système d'authentification sécurisé pour les utilisateurs.
- **Gestion des Mots de Passe**: Interface pour ajouter, consulter, mettre à jour et supprimer les mots de passe stockés.
- **Interface Simple**: Expérience utilisateur intuitive et facile à naviguer.

## Technologies Utilisées

- JavaFX
- JDBC
- MySQL (Base de données)
- WampServer (pour l'environnement serveur local)

## Instructions d'Installation

Pour mettre en place ce projet sur votre machine locale, suivez ces étapes :

1. **Installer WampServer**: Si vous ne l'avez pas déjà, téléchargez et installez WampServer depuis son site officiel. WampServer fournit Apache, MySQL et PHP sur votre machine Windows.

2. **Démarrer WampServer**: Lancez WampServer et assurez-vous que tous ses services (Apache et MySQL) sont en cours d'exécution. L'icône WampServer dans votre barre des tâches devrait devenir verte.

3. **Accéder à phpMyAdmin**: Ouvrez votre navigateur web et accédez à `http://localhost/phpmyadmin`. C'est l'interface web pour gérer vos bases de données MySQL.

4. **Créer la Base de Données**:

   - Dans phpMyAdmin, cliquez sur l'onglet "Bases de données".
   - Créez une nouvelle base de données nommée `password_manager`.

5. **Importer la Base de Données**:
   - Sélectionnez la base de données `password_manager` dans la barre latérale gauche.
   - Cliquez sur l'onglet "Importer".
   - Cliquez sur "Choisir un fichier" et sélectionnez le fichier `password_manager.sql` situé dans le répertoire database/ du projet.
   - Cliquez sur "Exécuter" pour importer le schéma de la base de données et les données initiales.

## Identifiants par Défaut du compte

- **Nom d'utilisateur**: `malik`
- **Mot de passe**: `malik`

## Utilisation

Après avoir configuré la base de données, vous pouvez exécuter l'application JavaFX. L'application se connectera à la base de données `password_manager` pour authentifier les utilisateurs et gérer les mots de passe.
