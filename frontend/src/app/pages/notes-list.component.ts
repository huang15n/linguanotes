import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { BehaviorSubject, catchError, combineLatest, debounceTime, of, startWith, switchMap, tap } from 'rxjs';
import { ApiService } from '../core/api.service';
import { UiService } from '../shared/dialogs';
import { Note, NoteFilters, NOTE_TYPES, typeLabel } from '../models';

@Component({
  imports: [ReactiveFormsModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatChipsModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatSlideToggleModule, MatProgressBarModule],
  styles: [`mat-card{margin-bottom:12px}`],
  template: `<div class="page">
    <div class="row"><h1 class="grow">Notes</h1><a mat-flat-button routerLink="/notes/new"><mat-icon>add</mat-icon> Nouvelle note</a></div>
    <form [formGroup]="form" class="row" style="margin-top:16px">
      <mat-form-field appearance="outline" class="grow"><mat-label>Rechercher (titre ou contenu)</mat-label><input matInput formControlName="q"><mat-icon matSuffix>search</mat-icon></mat-form-field>
      <mat-form-field appearance="outline"><mat-label>Langue</mat-label><mat-select formControlName="languageId"><mat-option [value]="null">Toutes</mat-option>@for (l of languages(); track l.id) { <mat-option [value]="l.id">{{ l.name }}</mat-option> }</mat-select></mat-form-field>
      <mat-form-field appearance="outline"><mat-label>Type</mat-label><mat-select formControlName="type"><mat-option [value]="null">Tous</mat-option>@for (t of types; track t.value) { <mat-option [value]="t.value">{{ t.label }}</mat-option> }</mat-select></mat-form-field>
      <mat-form-field appearance="outline"><mat-label>Unité</mat-label><input matInput formControlName="unit"></mat-form-field>
      <mat-form-field appearance="outline"><mat-label>Étiquette</mat-label><mat-select formControlName="tag"><mat-option [value]="null">Toutes</mat-option>@for (t of tags(); track t) { <mat-option [value]="t">{{ t }}</mat-option> }</mat-select></mat-form-field>
      <mat-slide-toggle formControlName="favorite">Favoris</mat-slide-toggle>
    </form>
    @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
    @for (n of notes(); track n.id) {
      <mat-card appearance="outlined">
        <mat-card-header><mat-card-title><a [routerLink]="['/notes', n.id]">{{ n.title }}</a></mat-card-title>
          <mat-card-subtitle>{{ n.languageName ?? 'Sans langue' }} · {{ tl(n.type) }}{{ n.unit ? ' · ' + n.unit : '' }}</mat-card-subtitle></mat-card-header>
        <mat-card-content>
          <p>{{ excerpt(n.content) }}</p>
          <mat-chip-set>@for (t of n.tags; track t) { <mat-chip>{{ t }}</mat-chip> }</mat-chip-set>
        </mat-card-content>
        <mat-card-actions align="end">
          <button mat-icon-button (click)="fav(n)" aria-label="Favori"><mat-icon>{{ n.favorite ? 'star' : 'star_border' }}</mat-icon></button>
          <a mat-button [routerLink]="['/notes', n.id, 'edit']">Modifier</a>
          <button mat-button (click)="remove(n)">Supprimer</button>
        </mat-card-actions>
      </mat-card>
    } @empty { @if (!loading()) { <p class="muted">Aucune note ne correspond.</p> } }
  </div>`
})
export class NotesListComponent {
  private api = inject(ApiService); private ui = inject(UiService);
  types = NOTE_TYPES; tl = typeLabel; loading = signal(true);
  languages = toSignal(this.api.languages(), { initialValue: [] }); tags = toSignal(this.api.tags(), { initialValue: [] });
  private refresh$ = new BehaviorSubject<void>(undefined);
  form = new FormGroup({
    q: new FormControl(''), languageId: new FormControl<number | null>(null), type: new FormControl<string | null>(null),
    unit: new FormControl(''), tag: new FormControl<string | null>(null), favorite: new FormControl(false) });
  notes = toSignal(combineLatest([this.form.valueChanges.pipe(startWith(this.form.value), debounceTime(250)), this.refresh$]).pipe(
    tap(() => this.loading.set(true)),
    switchMap(([f]) => this.api.notes(f as NoteFilters).pipe(catchError(() => of([] as Note[])))),
    tap(() => this.loading.set(false))), { initialValue: [] as Note[] });
  excerpt(md: string | null) { return (md ?? '').replace(/[#>*_`~|\[\]]|^-{3,}$|^[-+] /gm, '').replace(/\s+/g, ' ').trim().slice(0, 160); }
  fav(n: Note) { this.api.toggleFavorite(n.id).subscribe(() => this.refresh$.next()); }
  remove(n: Note) { this.ui.confirm(`Supprimer « ${n.title} » ?`).subscribe(() => this.api.deleteNote(n.id).subscribe(() => { this.ui.toast('Note supprimée'); this.refresh$.next(); })); }
}
