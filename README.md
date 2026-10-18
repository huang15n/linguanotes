# LinguaNotes — Angular 19 + Spring Boot 3 + MySQL 8

## Option 1 — Tout dans Docker (recommandé)

Prérequis : Docker Desktop installé et ouvert. Les valeurs Compose par défaut sont réservées au développement local.

    docker compose up --build

Puis ouvrir http://localhost:4200 (documentation API : http://localhost:8080/swagger-ui/index.html).
La première construction prend quelques minutes.

    docker compose down          # arrêter (les données sont conservées)
    docker compose down -v       # arrêter ET supprimer la base de données
    docker compose logs -f backend

Ports : 4200 (application), 8080 (API), 3307 (MySQL depuis l’hôte). MySQL et les documents importés utilisent des volumes Docker persistants.
Si le port 8080 ou 4200 est déjà utilisé, arrêtez l'ancien backend/frontend local.

Copiez `.env.example` vers `.env` et remplacez les valeurs avant tout déploiement. Définissez `COOKIE_SECURE=true` derrière HTTPS. Pour créer le premier administrateur, définissez `BOOTSTRAP_ADMIN_EMAIL` et `BOOTSTRAP_ADMIN_PASSWORD` (12 caractères minimum) au premier démarrage; l’inscription publique ne permet jamais de choisir un rôle privilégié.

## Option 2 — Développement local

    docker compose up -d mysql                # ou votre MySQL local, configuré par DB_URL
    cd backend  && mvn spring-boot:run        # Java 17+, http://localhost:8080
    cd frontend && npm install && npm start   # Node 20.19+ / 22, http://localhost:4200

Le backend lit `DB_URL`, `DB_USER`, `DB_PASSWORD`, `COOKIE_SECURE` et `FILE_STORAGE_DIR`. `SMTP_*` active la réinitialisation par courriel. `AI_API_KEY` active l’assistant IA côté serveur; sans clé, les endpoints IA renvoient une indisponibilité explicite.

## Fonctionnalités disponibles

- Interface française, espagnole, anglaise et chinoise simplifiée; préférence d’interface et objectif quotidien par compte.
- Inscription, connexion/déconnexion par session cookie HttpOnly avec protection CSRF, modification du profil et du mot de passe.
- Rôles `USER`, `TEACHER`, `ADMIN`; rôle administrateur initial facultatif via variables de bootstrap.
- Notes privées et hiérarchie cours → unités → leçons, avec filtrage de propriété côté backend.
- Vocabulaire multilingue avec exemples, IPA, étiquettes et difficulté.
- Quiz corrigés côté serveur, historique des tentatives, flashcards, révision espacée et progression.
- Import PDF textuel, DOCX, XLSX et XLS avec aperçu avant confirmation; maximum 20 Mo. Les PDF scannés nécessitent encore un OCR.
- Stockage fichier privé par compte et téléchargements autorisés par propriétaire.
- Assistant IA compatible OpenAI côté serveur, limité à 20 générations par utilisateur et par jour; les résultats sont des brouillons à vérifier.
- Export JSON et suppression de compte.
- Migrations MySQL Flyway versionnées, validation Hibernate au démarrage et OpenAPI/Swagger.

## Organisation backend

    controller/  contrôleurs REST
    service/     logique métier
    repository/  accès Spring Data JPA
    model/       entités et enums
    security/    principal et sessions
    config/      sécurité, configuration et initialisation
    dto/         contrats d’API

## Structure

    backend/   API REST Spring Boot, migrations Flyway, stockage privé + Dockerfile
    frontend/  Angular + Angular Material, i18n, routes protégées et pages par fonctionnalité
    docker-compose.yml
