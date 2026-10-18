import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Dashboard, Language, Note, NoteFilters, NoteRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private b = '/api';
  dashboard() { return this.http.get<Dashboard>(`${this.b}/dashboard`); }
  tags() { return this.http.get<string[]>(`${this.b}/tags`); }
  languages() { return this.http.get<Language[]>(`${this.b}/languages`); }
  createLanguage(body: Partial<Language>) { return this.http.post<Language>(`${this.b}/languages`, body); }
  updateLanguage(id: number, body: Partial<Language>) { return this.http.put<Language>(`${this.b}/languages/${id}`, body); }
  deleteLanguage(id: number) { return this.http.delete<void>(`${this.b}/languages/${id}`); }
  notes(f: NoteFilters = {}) {
    let p = new HttpParams();
    for (const [k, v] of Object.entries(f)) if (v !== null && v !== undefined && v !== '' && v !== false) p = p.set(k, String(v));
    return this.http.get<Note[]>(`${this.b}/notes`, { params: p });
  }
  note(id: string | number) { return this.http.get<Note>(`${this.b}/notes/${id}`); }
  createNote(body: NoteRequest) { return this.http.post<Note>(`${this.b}/notes`, body); }
  updateNote(id: string | number, body: NoteRequest) { return this.http.put<Note>(`${this.b}/notes/${id}`, body); }
  toggleFavorite(id: number) { return this.http.patch<Note>(`${this.b}/notes/${id}/favorite`, {}); }
  deleteNote(id: number) { return this.http.delete<void>(`${this.b}/notes/${id}`); }
}
