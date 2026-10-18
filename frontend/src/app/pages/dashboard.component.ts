import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatListModule } from '@angular/material/list';
import { ApiService } from '../core/api.service';
import { Dashboard, NOTE_TYPES, typeLabel } from '../models';

@Component({
  imports: [DatePipe, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatChipsModule, MatListModule],
  styles: [`.stat{display:flex;gap:16px;align-items:center;padding:16px}.n{font:var(--mat-sys-headline-medium)}`],
  template: `<div class="page">
    <div class="row"><h1 class="grow">Tableau de bord</h1><a mat-flat-button routerLink="/notes/new"><mat-icon>add</mat-icon> Nouvelle note</a></div>
    @if (data(); as d) {
      <div class="grid">
        <mat-card appearance="outlined"><mat-card-content class="stat"><mat-icon>description</mat-icon><div><div class="n">{{ d.totalNotes }}</div><div class="muted">Notes</div></div></mat-card-content></mat-card>
        <mat-card appearance="outlined"><mat-card-content class="stat"><mat-icon>language</mat-icon><div><div class="n">{{ d.totalLanguages }}</div><div class="muted">Langues</div></div></mat-card-content></mat-card>
        <mat-card appearance="outlined"><mat-card-content class="stat"><mat-icon>star</mat-icon><div><div class="n">{{ d.favorites }}</div><div class="muted">Favoris</div></div></mat-card-content></mat-card>
      </div>
      <h2>Par type</h2>
      <mat-chip-set>@for (t of types; track t.value) { <mat-chip>{{ t.label }} · {{ d.notesByType[t.value] ?? 0 }}</mat-chip> }</mat-chip-set>
      <h2>Récemment modifiées</h2>
      <mat-card appearance="outlined"><mat-nav-list>
        @for (n of d.recent; track n.id) {
          <a mat-list-item [routerLink]="['/notes', n.id]"><span matListItemTitle>{{ n.title }}</span>
            <span matListItemLine>{{ n.languageName ?? 'Sans langue' }} · {{ tl(n.type) }} · {{ n.updatedAt | date: 'medium' }}</span></a>
        } @empty { <p class="muted" style="padding:16px">Aucune note pour le moment.</p> }
      </mat-nav-list></mat-card>
    }</div>`
})
export class DashboardComponent implements OnInit {
  private api = inject(ApiService); data = signal<Dashboard | null>(null); types = NOTE_TYPES; tl = typeLabel;
  ngOnInit() { this.api.dashboard().subscribe(d => this.data.set(d)); }
}
