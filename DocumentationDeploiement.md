Procédure de Déploiement - Application EnAccord
Ce document décrit la stratégie de déploiement de l'application, le workflow Git associé, ainsi que la configuration du pipeline d'intégration et de déploiement continus (CI/CD).

# 1. Workflow de Versioning (Git Strategy)
   Le projet utilise deux branches principales pour séparer le développement de la production :

dev (Développement) : Branche sur laquelle les développeurs fusionnent leurs fonctionnalités validées en local.

main (Production) : Branche stable abritant le code prêt pour la production. Aucun développement direct ne s'y fait.

Cycle de vie d'une modification :
Développement de la fonctionnalité sur une branche locale.

Push et fusion vers la branche dev après validation des tests locaux.

Lorsque la version est prête pour la mise en production, création d'une Pull Request de dev vers main.

Validation et fusion sur main.

# 2. Déclenchement du CI/CD (Automatisation)
   Le pipeline CI/CD (exécuté via GitHub Actions) est configuré pour se déclencher uniquement lors d'un push ou d'une fusion sur la branche main.

Étapes du Pipeline :
Checkout du code : Récupération de la dernière version du code source depuis la branche main.

Configuration de l'environnement : Installation de Java (JDK) et configuration de Maven.

Exécution de la suite de tests : Lancement des tests unitaires et d'intégration via la commande :

mvn clean verify

Génération des rapports : Analyse de la couverture de code (JaCoCo).

Build de l'artefact : Génération du fichier exécutable (.jar).

Déploiement automatique : Transfert de l'artefact et redémarrage du service sur le serveur cible (ou publication de l'image conteneurisée).

# 3. Scripts de Déploiement et Configuration
   Fichier de configuration du Workflow (.github/workflows/ci-cd.yml)
   Voici le script automatisé gérant le CI/CD à chaque push sur main :

[Voir le fichier ci.yml](.github/workflows/ci.yml)

# 4. Liste des commandes pour récupérer du code depuis dev vers le dépot en passant par la branche main

### a. Vérifier que vous êtes bien sur la branche dev
git checkout dev

### b. Ajouter vos fichiers modifiés à l'index
git add .

### c. Créer un commit avec un message explicite
git commit -m "feat: ajout de la nouvelle fonctionnalité"

### d. Envoyer vos modifications locales sur la branche dev distante (GitHub)
git origin dev

# 5. Gestion des Secrets et Sécurisation
   Pour garantir la sécurité de l'application et respecter les bonnes pratiques de développement (évitant l'exposition de données sensibles dans le code source), aucune information critique (mots de passe de base de données, clés API, secrets JWT) n'est écrite en clair dans le dépôt Git.

### 1. Stockage sécurisé (GitHub Secrets)
   Les données sensibles sont externalisées et stockées de manière chiffrée directement dans les paramètres de sécurité du dépôt GitHub :

Chemin : Settings > Secrets and variables > Actions

Variables configurées :

DB_HOST : Adresse du serveur de base de données de production.

DB_USER : Nom d'utilisateur de la base de données.

DB_PASSWORD : Mot de passe administrateur de la base de données.

JWT_SECRET : Clé de chiffrement pour la génération des tokens d'authentification.

### 2. Injection des secrets dans le Pipeline CI/CD
   Lors du déclenchement du pipeline sur la branche main, les secrets sont injectés dynamiquement sous forme de variables d'environnement au moment du build ou du lancement de l'application :

YAML
- name: Étape - Lancement avec injection des secrets
env:
SPRING_DATASOURCE_URL: ${{ secrets.DB_HOST }}
SPRING_DATASOURCE_USERNAME: ${{ secrets.DB_USER }}
SPRING_DATASOURCE_PASSWORD: ${{ secrets.DB_PASSWORD }}
JWT_SECRET_KEY: ${{ secrets.JWT_SECRET }}
run: |
mvn spring-boot:run -Dspring-boot.run.profiles=prod
### 3. Isolation en local
   Pour les développeurs travaillant sur la branche dev, les secrets sont stockés dans un fichier local application-dev.properties exclu des commits grâce au fichier .gitignore (garantissant l'absence de fuite accidentelle dans l'historique Git).