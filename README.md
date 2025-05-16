Analyse structurée des projets Tbibi
Tbibi - JavaFX (Application Desktop)

1.1 Description
Tbibi (JavaFX) est une application desktop développée avec JavaFX pour gérer les services médicaux. Elle facilite la gestion des rendez-vous, dossiers médicaux et calendriers pour les médecins, patients et administrateurs via une interface graphique intuitive.

1.2 Fonctionnalités
Gestion des utilisateurs :
Création, modification, suppression de comptes (médecins, patients, administrateurs).
Permissions basées sur les rôles.
Prise de rendez-vous :
Planification par patients ou administrateurs.
Gestion (consultation, modification) par médecins.
Gestion des dossiers médicaux :
Stockage des antécédents, diagnostics, prescriptions.
Accès sécurisé (médecins : édition, patients : consultation).
Calendrier des événements :
Visualisation interactive des rendez-vous.
Support potentiel pour notifications/rappels.
Spécialités médicales :
Gestion des spécialités via énumérations.
Filtrage des médecins par spécialité.
Interface utilisateur :
Intuitive, responsive, stylée avec CSS.
1.3 Prérequis
Java JDK : Version 17 ou supérieure.
MySQL : Version 8.0 ou supérieure.
Maven : Version 3.8 ou supérieure.
1.4 Installation
Configuration de la base de données :
Créer une base de données MySQL nommée tbibi2 :
sql

Copier
CREATE DATABASE tbibi2;
Importer le script SQL :
bash

Copier
mysql -u [username] -p tbibi2 < database/tbibi2.sql
Clonage du dépôt :
bash

Copier
git clone https://github.com/votre-username/Tbibi-JavaFx.git
cd Tbibi-JavaFx
Configuration de la connexion :
Modifier src/main/java/utils/MyDataBase.java avec les paramètres MySQL (hôte, utilisateur, mot de passe).
Compilation et exécution :
bash

Copier
mvn clean javafx:run
1.5 Utilisation
Lancement : Exécuter l’application via Maven.
Connexion :
Médecins : Accès au calendrier, gestion des rendez-vous, dossiers patients.
Patients : Prise de rendez-vous, consultation des dossiers.
Administrateurs : Gestion des utilisateurs, statistiques.
Interface : Navigation via menus JavaFX, avec vues adaptées aux rôles.
1.6 Structure du projet
text

Copier
Tbibi-JavaFx/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── controller/    # Contrôleurs JavaFX (logique UI)
│   │   │   ├── enums/         # Énumérations (ex. : spécialités)
│   │   │   ├── model/         # Classes de données (ex. : Patient, RendezVous)
│   │   │   ├── service/       # Logique métier (ex. : gestion rendez-vous)
│   │   │   └── utils/         # Utilitaires (ex. : connexion DB)
│   │   └── resources/
│   │       ├── css/           # Styles visuels
│   │       ├── fxml/          # Interfaces graphiques (FXML)
│   │       └── images/        # Icônes et images
└── database/                  # Scripts SQL (tbibi2.sql)
1.7 Technologies
JavaFX : Interface utilisateur graphique.
MySQL : Stockage des données.
Maven : Gestion des dépendances.
PDFBox : Génération de documents PDF.
Jackson : Traitement JSON (possiblement pour API ou exports).
OkHttp : Requêtes HTTP (intégrations externes).
1.8 Auteurs et remerciements
Développeur principal : [Votre nom].
Contributeurs : [Autres contributeurs, non précisés].
1.9 Licence
Non spécifiée (à clarifier pour contributions).
1.10 Statut du projet
État : En développement actif.
Contributions : Bienvenues (dépôt GitHub).
Tbibi - Symfony (Plateforme Web)

2.1 Description
Tbibi (Symfony) est une plateforme web de santé connectée développée avec Symfony 6.4. Elle facilite la communication entre patients et médecins via un forum, propose des services comme la synthèse vocale des réponses et un catalogue de produits médicaux.

2.2 Fonctionnalités
Forum de questions/réponses :
Questions médicales par spécialité.
Réponses validées par médecins.
Synthèse vocale :
Conversion des réponses textuelles en audio (via Web Speech API).
Gestion des profils :
Profils détaillés pour médecins et patients.
Catalogue de produits :
Liste de produits médicaux à commander.
Système de commande :
Gestion des achats et suivi.
Suivi médical :
Historique personnalisé des interactions patient-médecin.
Administration :
Gestion des utilisateurs, modération du forum, statistiques.
2.3 Prérequis
PHP : Version 8.1 ou supérieure.
Symfony : Version 6.4.
MySQL/MariaDB : Version 10.4 ou supérieure.
Composer : Gestion des dépendances PHP.
Node.js/npm : Compilation des assets (CSS, JS).
2.4 Installation
Clonage du dépôt :
bash

Copier
git clone https://github.com/votre-username/tbibi.git
cd tbibi
Installation des dépendances :
bash

Copier
composer install
npm install
npm run build
Configuration de la base de données :
Modifier .env :
text

Copier
DATABASE_URL="mysql://username:password@127.0.0.1:3306/tbibi2?serverVersion=10.4.32-MariaDB"
Créer la base de données :
bash

Copier
php bin/console doctrine:database:create
Exécuter les migrations :
bash

Copier
php bin/console doctrine:migrations:migrate
Charger les données de test :
bash

Copier
php bin/console doctrine:fixtures:load
Démarrage du serveur :
bash

Copier
symfony server:start
2.5 Utilisation
Accès : Via http://localhost:8000.
Comptes de test :
Admin : admin@tbibi.com / password.
Médecin : medecin@tbibi.com / password.
Patient : patient@tbibi.com / password.
Fonctionnalités par rôle :
Patients : Poser des questions, consulter réponses, commander produits.
Médecins : Répondre aux questions, gérer profil.
Administrateurs : Modérer contenu, gérer utilisateurs.
2.6 Structure du projet
text

Copier
tbibi/
├── src/
│   ├── Controller/    # Contrôleurs Symfony
│   ├── Entity/        # Entités Doctrine (ex. : User, Question)
│   ├── Form/          # Formulaires (ex. : formulaire de question)
│   ├── Repository/    # Requêtes personnalisées
│   └── Enum/          # Énumérations (ex. : spécialités)
├── templates/         # Templates Twig (vues HTML)
├── public/            # Fichiers statiques (CSS, JS, images)
2.7 Technologies
Symfony 6.4 : Framework web.
Doctrine ORM : Gestion des entités et base de données.
Twig : Moteur de templates.
Bootstrap : Styles frontend.
SendGrid : Envoi d’emails.
Twilio : Envoi de SMS.
Web Speech API : Synthèse vocale.
2.8 Auteurs et remerciements
Développeur principal : [Votre nom].
Contributeurs : [Autres contributeurs, non précisés].
2.9 Licence
Non spécifiée (à clarifier).
2.10 Statut du projet
État : En développement actif.
Branches : La mention "mettre les branch au milieu du cette paragraphe" semble être une erreur ou une instruction inachevée. Aucune branche spécifique n’est listée (ex. : main, dev). À vérifier dans le dépôt GitHub.
Contributions : Bienvenues.
Comparaison des projets
Similitudes
Objectif : Gestion médicale et interaction patient-médecin.
Base de données : Utilisation de tbibi2 (MySQL/MariaDB).
Utilisateurs : Médecins, patients, administrateurs.
Statut : En développement actif, contributions bienvenues.
Différences

Critère	Tbibi (JavaFX)	Tbibi (Symfony)
Type	Application desktop	Plateforme web
Fonctionnalités	Rendez-vous, dossiers, calendrier	Forum, synthèse vocale, catalogue
Technologies	JavaFX, MySQL, Maven, PDFBox	Symfony, Doctrine, Twig, SendGrid
Déploiement	Local (exécution via Maven)	Serveur web (Symfony)
Interface	Graphique (FXML/CSS)	Web (Twig/Bootstrap)
Clarification sur les branches (Symfony)
La phrase "mettre les branch au milieu du cette paragraphe" dans la description Symfony semble être une erreur. Sans accès au dépôt GitHub, je ne peux pas lister les branches. Pour vérifier :

Clonez le dépôt.
Exécutez :
bash

Copier
git branch -a
Vérifiez les branches disponibles (ex. : main, develop).
Recommandations
Choix du projet :
Choisissez JavaFX si vous avez besoin d’une application desktop pour une gestion locale (ex. : clinique).
Choisissez Symfony pour une plateforme web accessible à distance avec interactions dynamiques.
Installation :
Assurez-vous d’avoir les versions correctes des prérequis (ex. : JDK 17, PHP 8.1).
Testez avec les comptes fournis pour Symfony.
Contribution :
Clarifiez la licence pour éviter des problèmes juridiques.
Vérifiez les issues sur GitHub (URL non valide actuellement).
Problèmes potentiels :
URLs GitHub génériques (votre-username). Fournissez les vraies URLs pour une analyse approfondie.
Configuration MySQL peut échouer si les credentials ne sont pas corrects.
Conclusion
Tbibi JavaFX : Idéal pour une gestion médicale locale avec une interface desktop robuste.
Tbibi Symfony : Convient pour une plateforme web interactive avec des fonctionnalités modernes comme la synthèse vocale.
Si vous souhaitez des détails supplémentaires (ex. : analyse de code, comparaison technique, ou aide à l’installation), fournissez plus de contexte ou une URL GitHub valide. Pour gérer la mémoire de cette conversation, utilisez l’icône livre sous le message ou désactivez la mémoire dans "Data Controls".
github.com
