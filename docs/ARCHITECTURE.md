# Architecture de LinguaNotes

Ce document décrit l'organisation du dépôt et les conventions à respecter
lors d'une évolution.

## Vue d'ensemble

```
linguanotes/
├── backend/                 API REST Spring Boot 3 / Java 17
├── frontend/                Application Angular 19 (standalone components)
├── docs/                    Documentation technique
├── docker-compose.yml       MySQL + backend + frontend (nginx)
├── .env.example             Variables d'environnement documentées
└── README.md                Démarrage rapide
```

Chaque application est autonome : elle possède son propre `Dockerfile`, ses
dépendances et ses tests. Le frontend est servi par nginx qui relaie `/api/`
vers le backend, ce qui évite le CORS en production.

## Backend — architecture en couches

```
com.linguanotes
├── LinguaNotesApplication.java
├── config/            SecurityConfig, WebConfig, OpenApiConfig, DataInitializer
├── controller/        Contrôleurs REST (un par ressource)
├── dto/               Un record par contrat d'API
├── error/             Gestion globale des exceptions
├── model/             Entités JPA et énumérations
├── repository/        Interfaces Spring Data JPA
├── security/          Principal Spring Security et UserDetailsService
└── service/           Logique métier et règles d'accès
```

Règles :

- Les contrôleurs ne contiennent pas de logique métier ; ils délèguent aux
  services et exposent uniquement des DTO.
- Chaque DTO est un `record` autonome dans `dto/` (pas de classe « fourre-tout »).
- Les services vérifient systématiquement la propriété des ressources
  (`ownerId`) avant toute lecture ou écriture.
- Les erreurs métier utilisent `ResponseStatusException` et sont converties en
  JSON `{ "message": "..." }` par `GlobalExceptionHandler`.
- Toute évolution du schéma passe par une migration Flyway versionnée dans
  `src/main/resources/db/migration`. Ne jamais modifier une migration déjà
  appliquée.
- Les entités utilisent Lombok (`@Getter`, `@Setter`, `@NoArgsConstructor`) ;
  pas de logique métier dans les entités.

### Sécurité

- Authentification par session HTTP (`JSESSIONID`) et cookie `HttpOnly`.
- Protection CSRF par cookie `XSRF-TOKEN` + en-tête `X-XSRF-TOKEN`.
- Mots de passe hachés en BCrypt (coût 12).
- Rôles `USER`, `TEACHER`, `ADMIN` ; `/api/admin/**` réservé aux admins.
- Les fichiers sont stockés hors du web root et téléchargés uniquement par
  leur propriétaire.

### Tests

```bash
cd backend
mvn test
```

Les tests unitaires mockent les repositories. `src/test/resources/mockito-extensions`
force le mock maker « subclass » afin que la suite fonctionne aussi dans les
environnements où l'auto-attachement de l'agent Byte Buddy est bloqué.

## Frontend — architecture par fonctionnalité

```
src/app/
├── core/
│   ├── guards/          authGuard, adminGuard
│   ├── i18n/            Catalogues fr, es, en, zh-CN
│   ├── interceptors/    Gestion centralisée des erreurs HTTP
│   ├── models/          Modèles TypeScript par domaine + barrel index.ts
│   └── services/        ApiService, AuthService, LocaleService, ThemeService
├── shared/
│   ├── dialogs/         Dialogues Material réutilisables et UiService
│   └── markdown/        Éditeur et pipe Markdown
├── features/
│   ├── admin/           Gestion des comptes (ADMIN)
│   ├── ai/              Assistant IA
│   ├── auth/            Connexion, inscription, réinitialisation
│   ├── courses/         Cours → unités → leçons
│   ├── dashboard/       Tableau de bord
│   ├── exercises/       Quiz multi-questions
│   ├── files/           Fichiers privés (documents et audio)
│   ├── imports/         Import PDF/DOCX/XLSX
│   ├── languages/       Référentiel des langues
│   ├── notes/           Liste, détail et formulaire de notes
│   ├── not-found/       Page 404
│   ├── progress/        Statistiques d'apprentissage
│   ├── review/          Flashcards et répétition espacée
│   ├── settings/        Profil, sécurité, préférences, données
│   └── vocabulary/      Fiches de vocabulaire
├── app.component.*      Coquille de l'application (navigation)
├── app.config.ts        Providers globaux, CSRF, initialisation
└── app.routes.ts        Routage lazy-loading
```

Règles :

- Les composants sont standalone et importent explicitement leurs dépendances.
- Les composants ne parlent jamais directement à `HttpClient` : tout passe par
  `ApiService`.
- Les modèles sont regroupés par domaine dans `core/models/` et réexportés par
  `index.ts`.
- Toutes les chaînes visibles passent par la clé i18n et le pipe `t`.
- Les erreurs HTTP sont affichées une seule fois par l'intercepteur ; les
  formulaires gèrent leurs erreurs de validation localement.
- Les routes protégées utilisent `authGuard` ; `/admin` utilise `adminGuard`.
- Les confirmations destructives utilisent `UiService.confirm`, jamais
  `window.confirm`.

### Build et vérification

```bash
cd frontend
npm ci
node node_modules/typescript/bin/tsc -p tsconfig.app.json --noEmit
npm run build
```

## Conventions Git

- Un commit par intention (fonctionnalité, correction, refactor).
- Messages à l'impératif, en français ou en anglais, mais cohérents.
- Ne jamais committer `.env`, `node_modules`, `target`, `dist` ou `.angular`.
