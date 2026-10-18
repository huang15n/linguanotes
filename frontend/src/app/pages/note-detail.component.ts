import { Component, inject, input, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MarkdownPipe } from '../shared/markdown.pipe';
import { ApiService } from '../core/api.service';
import { UiService } from '../shared/dialogs';
import { Note, typeLabel } from '../models';

@Component({
  imports: [MarkdownPipe, DatePipe, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatChipsModule],
  template: `<div class="page"><a mat-button routerLink="/notes"><mat-icon>arrow_back</mat-icon> Notes</a>
    @if (note(); as n) {
      <div class="row"><h1 class="grow">{{ n.title }}</h1>
        <button mat-icon-button (click)="fav()" aria-label="Favori"><mat-icon>{{ n.favorite ? 'star' : 'star_border' }}</mat-icon></button>
        <a mat-stroked-button [routerLink]="['/notes', n.id, 'edit']"><mat-icon>edit</mat-icon> Modifier</a>
        <button mat-button (click)="remove()"><mat-icon>delete</mat-icon> Supprimer</button></div>
      <mat-chip-set><mat-chip>{{ n.languageName ?? 'Sans langue' }}</mat-chip><mat-chip>{{ tl(n.type) }}</mat-chip>
        @if (n.unit) { <mat-chip>{{ n.unit }}</mat-chip> }@for (t of n.tags; track t) { <mat-chip>#{{ t }}</mat-chip> }</mat-chip-set>
      <mat-card appearance="outlined" style="margin-top:16px"><mat-card-content><div class="md" [innerHTML]="n.content | markdown"></div></mat-card-content></mat-card>
      <p class="muted">Créée le {{ n.createdAt | date: 'medium' }} · modifiée le {{ n.updatedAt | date: 'medium' }}</p>
    }</div>`
})
export class NoteDetailComponent implements OnInit {
  id = input.required<string>(); private api = inject(ApiService); private ui = inject(UiService); private router = inject(Router);
  note = signal<Note | null>(null); tl = typeLabel;
  ngOnInit() { this.api.note(this.id()).subscribe(n => this.note.set(n)); }
  fav() { this.api.toggleFavorite(+this.id()).subscribe(n => this.note.set(n)); }
  remove() { this.ui.confirm('Supprimer cette note ?').subscribe(() => this.api.deleteNote(+this.id()).subscribe(() => { this.ui.toast('Note supprimée'); this.router.navigate(['/notes']); })); }
}
