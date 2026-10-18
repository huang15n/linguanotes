import { Routes } from '@angular/router';
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'dashboard', loadComponent: () => import('./pages/dashboard.component').then(m => m.DashboardComponent) },
  { path: 'notes', loadComponent: () => import('./pages/notes-list.component').then(m => m.NotesListComponent) },
  { path: 'notes/new', loadComponent: () => import('./pages/note-form.component').then(m => m.NoteFormComponent) },
  { path: 'notes/:id', loadComponent: () => import('./pages/note-detail.component').then(m => m.NoteDetailComponent) },
  { path: 'notes/:id/edit', loadComponent: () => import('./pages/note-form.component').then(m => m.NoteFormComponent) },
  { path: 'languages', loadComponent: () => import('./pages/languages.component').then(m => m.LanguagesComponent) },
  { path: '**', redirectTo: 'dashboard' },
];
