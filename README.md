# LinguaNotes — Angular 19 + Spring Boot 3 + MySQL 8 (MVP)

## Option 1 — Tout dans Docker (recommandé)
Prérequis : Docker Desktop installé et ouvert.

    docker compose up --build

Puis ouvrir http://localhost:4200 (API directe : http://localhost:8080/api/languages).
La première construction prend quelques minutes.

    docker compose down          # arrêter (les données sont conservées)
    docker compose down -v       # arrêter ET supprimer la base de données
    docker compose logs -f backend

Ports : 4200 (application), 8080 (API), 3307 (MySQL depuis le Mac : mysql -h 127.0.0.1 -P 3307 -uroot -proot).
Si le port 8080 ou 4200 est déjà utilisé, arrêtez l'ancien backend/frontend local.

## Option 2 — Développement local
    docker compose up -d mysql                # ou votre MySQL local (root/root, port 3306 -> DB_URL)
    cd backend  && mvn spring-boot:run        # Java 17+, http://localhost:8080
    cd frontend && npm install && npm start   # Node 20.19+ / 22, http://localhost:4200

Avec le MySQL de Docker (port 3307) : DB_URL="jdbc:mysql://localhost:3307/linguanotes_app?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" mvn spring-boot:run

## Structure
    backend/   API REST Spring Boot (notes, langues, étiquettes, tableau de bord) + Dockerfile
    frontend/  Angular + Angular Material, éditeur Markdown + Dockerfile (nginx)
    docker-compose.yml
