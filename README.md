# LinguaNotes

Application d'apprentissage des langues : notes structurées, cours, vocabulaire,
quiz, flashcards avec répétition espacée et assistant IA.

- **Frontend** : Angular 19, Angular Material, composants standalone, i18n FR/ES/EN/ZH.
- **Backend** : Spring Boot 3.3, Spring Security (session + CSRF), JPA, Flyway, OpenAPI.
- **Base de données** : MySQL 8.4.
- **Déploiement** : Docker Compose (nginx sert le frontend et relaie `/api/`).

## Démarrage rapide (Docker)

Prérequis : Docker Desktop.

```bash
docker compose up --build
```

- Application : http://localhost:4200
- Documentation API : http://localhost:8080/swagger-ui/index.html

La première construction prend quelques minutes. Les données MySQL et les
fichiers importés sont conservés dans des volumes Docker.

```bash
docker compose down       # arrêter (données conservées)
docker compose down -v    # arrêter et supprimer la base
docker compose logs -f backend
```

## Développement local

Prérequis : Java 17+, Maven 3.9+, Node 20.19+/22+, MySQL 8 (ou Docker).

```bash
# 1. Base de données
docker compose up -d mysql

# 2. API (http://localhost:8080)
cd backend
mvn spring-boot:run

# 3. Frontend (http://localhost:4200)
cd frontend
npm ci
npm start
```

Le proxy Angular (`proxy.conf.json`) relaie `/api` vers le backend ; aucun CORS
n'est nécessaire en développement.

### Vérifications

```bash
cd backend  && mvn test
cd frontend && npm run build
```

## Configuration

Copiez `.env.example` vers `.env` et adaptez les valeurs. Les valeurs par défaut
sont réservées au développement local.

| Variable | Rôle | Défaut |
| --- | --- | --- |
| `DB_URL` | URL JDBC MySQL | `jdbc:mysql://localhost:3306/linguanotes_app...` |
| `DB_USER` / `DB_PASSWORD` | Identifiants MySQL | selon votre installation locale |
| `COOKIE_SECURE` | Cookie de session HTTPS uniquement | `false` |
| `APP_PUBLIC_URL` | URL publique (liens de réinitialisation) | `http://localhost:4200` |
| `FILE_STORAGE_DIR` | Stockage privé des fichiers | `./data/files` |
| `AI_API_KEY` | Clé OpenAI-compatible ; vide = IA désactivée | — |
| `SMTP_*` | Envoi des courriels de réinitialisation | — |
| `BOOTSTRAP_ADMIN_EMAIL` / `_PASSWORD` | Création du premier administrateur | — |

Définissez `COOKIE_SECURE=true` derrière HTTPS. Le compte administrateur initial
n'est créé que si l'adresse n'existe pas déjà ; le mot de passe doit contenir au
moins 12 caractères.

## Fonctionnalités

- Inscription, connexion/déconnexion par session HttpOnly avec protection CSRF.
- Rôles `USER`, `TEACHER`, `ADMIN` et page d'administration des comptes.
- Notes : Markdown H1–H5, images, sauvegarde automatique avec brouillon local,
  niveaux par langue, visibilité, étiquettes, favoris, J'aime et commentaires.
- Référentiel de niveaux par langue : IELTS pour l'anglais, A1–C2 pour le français
  et l'espagnol, traditionnel/simplifié pour le chinois.
- Cours → unités → leçons → sections pédagogiques flexibles (dialogue, grammaire,
  vocabulaire, phonétique, culture, exercices, référence, révisions…).
- Vocabulaire enrichi : type grammatical, genre, définition, IPA, exemples,
  synonymes, antonymes, statut d'apprentissage et tableaux de conjugaison.
- Import/export CSV et Excel (.xlsx) du vocabulaire avec rapport d'importation
  et feuille de conjugaisons dédiée.
- Quiz paramétrables : langue, niveau, notion, introduction, statut, seuil de
  réussite, nombre de tentatives et mode aperçu ; correction côté serveur.
- Révisions : sélection de mots par langue/niveau/statut, cartes, traduction,
  choix multiple et enregistrement des sessions.
- Classes : création par un enseignant/administrateur, code d'invitation,
  inscription des élèves et gestion des membres.
- Profil complet : prénom/nom, nom d'affichage, pays, fuseau horaire, langue
  maternelle et langues étudiées avec niveaux.
- Administration : recherche, filtres par rôle/statut, changement de rôle et
  suspension de compte.
- Tableau de bord et progression repensés, thème clair/sombre, interface
  FR/ES/EN/ZH-CN.
- Import PDF textuel, DOCX, XLSX et XLS avec aperçu avant confirmation.
- Gestion des fichiers privés (documents, images et audio), téléchargement contrôlé.
- Assistant IA compatible OpenAI, limité à 20 générations par utilisateur/jour.
- Export JSON et suppression de compte.
- Migrations Flyway V1–V10, validation Hibernate et OpenAPI/Swagger.

## Structure du dépôt

```
backend/                  API Spring Boot
  src/main/java/com/linguanotes/
    config/               Sécurité, CORS, OpenAPI, données initiales
    controller/           Contrôleurs REST par ressource
    dto/                  Un record par contrat d'API
    error/                Gestion globale des exceptions
    model/                Entités JPA et énumérations
    repository/           Spring Data JPA
    security/             Principal et UserDetailsService
    service/              Logique métier
  src/main/resources/db/migration/   Migrations Flyway
  src/test/               Tests unitaires

frontend/                 Application Angular
  src/app/
    core/                 Guards, i18n, interceptors, modèles, services
    shared/               Dialogues et éditeur Markdown
    features/             Notes, cours, vocabulaire, révisions, exercices,
                          progression, fichiers, étiquettes, administration…
    features/             Un dossier par fonctionnalité
    app.routes.ts         Routage lazy-loading
    app.config.ts         Providers, CSRF, initialisation

docs/ARCHITECTURE.md      Conventions détaillées
docker-compose.yml        MySQL + backend + frontend
```

Les conventions de code et l'organisation détaillée sont décrites dans
[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).
