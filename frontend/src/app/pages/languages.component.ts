import { Component, inject, OnInit, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ApiService } from '../core/api.service';
import { UiService } from '../shared/dialogs';
import { Language } from '../models';

@Component({
  imports: [MatCardModule, MatButtonModule, MatIconModule],
  template: `<div class="page"><div class="row"><h1 class="grow">Langues</h1><button mat-flat-button (click)="edit()"><mat-icon>add</mat-icon> Nouvelle langue</button></div>
    <div class="grid">@for (l of list(); track l.id) {
      <mat-card appearance="outlined"><mat-card-header><mat-card-title>{{ l.name }}</mat-card-title><mat-card-subtitle>{{ l.code }}</mat-card-subtitle></mat-card-header>
        <mat-card-actions align="end"><button mat-button (click)="edit(l)">Modifier</button><button mat-button (click)="remove(l)">Supprimer</button></mat-card-actions></mat-card>
    } @empty { <p class="muted">Aucune langue.</p> }</div></div>`
})
export class LanguagesComponent implements OnInit {
  private api = inject(ApiService); private ui = inject(UiService); list = signal<Language[]>([]);
  ngOnInit() { this.load(); }
  load() { this.api.languages().subscribe(l => this.list.set(l)); }
  edit(l?: Language) {
    this.ui.form({ title: l ? 'Modifier la langue' : 'Nouvelle langue', fields: [{ key: 'name', label: 'Nom' }, { key: 'code', label: 'Code (es, fr, en…)' }], value: l ? { name: l.name, code: l.code } : undefined })
      .subscribe(v => (l ? this.api.updateLanguage(l.id, v) : this.api.createLanguage(v)).subscribe(() => { this.ui.toast('Langue enregistrée'); this.load(); }));
  }
  remove(l: Language) { this.ui.confirm(`Supprimer « ${l.name} » ?`).subscribe(() => this.api.deleteLanguage(l.id).subscribe(() => { this.ui.toast('Langue supprimée'); this.load(); })); }
}
