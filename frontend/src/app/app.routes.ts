import { Routes } from "@angular/router";
import { adminGuard, authGuard } from "./core/guards/auth.guard";

export const routes: Routes = [
  { path: "", pathMatch: "full", redirectTo: "dashboard" },
  // Les routes statiques doivent précéder /auth/:mode.
  {
    path: "auth/reset",
    loadComponent: () =>
      import("./features/auth/password-reset.component").then(
        (m) => m.PasswordResetComponent,
      ),
  },
  {
    path: "auth/:mode",
    loadComponent: () =>
      import("./features/auth/auth-page.component").then(
        (m) => m.AuthPageComponent,
      ),
  },
  {
    path: "dashboard",
    canActivate: [authGuard],
    loadComponent: () =>
      import("./features/dashboard/dashboard.component").then(
        (m) => m.DashboardComponent,
      ),
  },
  {
    path: "notes",
    canActivate: [authGuard],
    loadComponent: () =>
      import("./features/notes/notes-list.component").then(
        (m) => m.NotesListComponent,
      ),
  },
  {
    path: "notes/new",
    canActivate: [authGuard],
    loadComponent: () =>
      import("./features/notes/note-form.component").then(
        (m) => m.NoteFormComponent,
      ),
  },
  {
    path: "notes/:id",
    canActivate: [authGuard],
    loadComponent: () =>
      import("./features/notes/note-detail.component").then(
        (m) => m.NoteDetailComponent,
      ),
  },
  {
    path: "notes/:id/edit",
    canActivate: [authGuard],
    loadComponent: () =>
      import("./features/notes/note-form.component").then(
        (m) => m.NoteFormComponent,
      ),
  },
  {
    path: "languages",
    canActivate: [authGuard],
    loadComponent: () =>
      import("./features/languages/languages.component").then(
        (m) => m.LanguagesComponent,
      ),
  },
  {
    path: "courses",
    canActivate: [authGuard],
    loadComponent: () =>
      import("./features/courses/courses.component").then(
        (m) => m.CoursesComponent,
      ),
  },
  {
    path: "vocabulary",
    canActivate: [authGuard],
    loadComponent: () =>
      import("./features/vocabulary/vocabulary.component").then(
        (m) => m.VocabularyComponent,
      ),
  },
  {
    path: "exercises",
    canActivate: [authGuard],
    loadComponent: () =>
      import("./features/exercises/exercises.component").then(
        (m) => m.ExercisesComponent,
      ),
  },
  {
    path: "review",
    canActivate: [authGuard],
    loadComponent: () =>
      import("./features/review/review.component").then(
        (m) => m.ReviewComponent,
      ),
  },
  {
    path: "progress",
    canActivate: [authGuard],
    loadComponent: () =>
      import("./features/progress/progress.component").then(
        (m) => m.ProgressComponent,
      ),
  },
  {
    path: "imports",
    canActivate: [authGuard],
    loadComponent: () =>
      import("./features/imports/imports.component").then(
        (m) => m.ImportsComponent,
      ),
  },
  {
    path: "files",
    canActivate: [authGuard],
    loadComponent: () =>
      import("./features/files/files.component").then(
        (m) => m.FilesComponent,
      ),
  },
  {
    path: "ai",
    canActivate: [authGuard],
    loadComponent: () =>
      import("./features/ai/ai.component").then((m) => m.AiComponent),
  },
  {
    path: "admin",
    canActivate: [adminGuard],
    loadComponent: () =>
      import("./features/admin/admin-users.component").then(
        (m) => m.AdminUsersComponent,
      ),
  },
  {
    path: "settings",
    canActivate: [authGuard],
    loadComponent: () =>
      import("./features/settings/settings.component").then(
        (m) => m.SettingsComponent,
      ),
  },
  {
    path: "**",
    loadComponent: () =>
      import("./features/not-found/not-found.component").then(
        (m) => m.NotFoundComponent,
      ),
  },
];
