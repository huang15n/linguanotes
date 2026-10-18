import { Component, inject, input, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MarkdownEditorComponent } from '../shared/markdown-editor.component';
import { ApiService } from '../core/api.service';
import { UiService } from '../shared/dialogs';
import { Language, NOTE_TYPES, NoteRequest, NoteType } from '../models';

@Component({
  imports: [MarkdownEditorComponent, ReactiveFormsModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatSlideToggleModule],
  template: `<div class="page"><a mat-button routerLink="/notes"><mat-icon>arrow_back</mat-icon> Notes</a>
    <h1>{{ id() ? 'Modifier la note' : 'Nouvelle note' }}</h1>
    <mat-card appearance="outlined"><mat-card-content><form [formGroup]="form" class="stack" style="padding-top:16px">
      <mat-form-field appearance="outline" style="width:100%"><mat-label>Titre</mat-label><input matInput formControlName="title">
        @if (form.controls.title.hasError('required')) { <mat-error>Le titre est obligatoire</mat-error> }</mat-form-field>
      <div class="row">
        <mat-form-field appearance="outline" class="grow"><mat-label>Langue</mat-label><mat-select formControlName="languageId"><mat-option [value]="null">Aucune</mat-option>@for (l of languages(); track l.id) { <mat-option [value]="l.id">{{ l.name }}</mat-option> }</mat-select></mat-form-field>
        <mat-form-field appearance="outline" class="grow"><mat-label>Type</mat-label><mat-select formControlName="type">@for (t of types; track t.value) { <mat-option [value]="t.value">{{ t.label }}</mat-option> }</mat-select></mat-form-field>
        <mat-form-field appearance="outline" class="grow"><mat-label>Unité (ex. Unité 3)</mat-label><input matInput formControlName="unit"></mat-form-field>
      </div>
      <mat-form-field appearance="outline" style="width:100%"><mat-label>Étiquettes (séparées par des virgules)</mat-label><input matInput formControlName="tags" placeholder="verbes, A1"></mat-form-field>
      <app-markdown-editor formControlName="content" />
      <mat-slide-toggle formControlName="favorite">Favori</mat-slide-toggle>
    </form></mat-card-content>
    <mat-card-actions align="end"><a mat-button routerLink="/notes">Annuler</a><button mat-flat-button (click)="save()" [disabled]="form.invalid"><mat-icon>save</mat-icon> Enregistrer</button></mat-card-actions></mat-card></div>`
})
export class NoteFormComponent implements OnInit {
  id = input<string>(); private api = inject(ApiService); private ui = inject(UiService); private router = inject(Router);
  languages = signal<Language[]>([]); types = NOTE_TYPES;
  form = new FormGroup({
    title: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(255)] }),
    languageId: new FormControl<number | null>(null), type: new FormControl<NoteType>('OTHER', { nonNullable: true }),
    unit: new FormControl('', { nonNullable: true }), tags: new FormControl('', { nonNullable: true }),
    favorite: new FormControl(false, { nonNullable: true }), content: new FormControl('', { nonNullable: true }) });
  ngOnInit() {
    this.api.languages().subscribe(l => this.languages.set(l));
    const id = this.id();
    if (id) this.api.note(id).subscribe(n => this.form.patchValue({ title: n.title, languageId: n.languageId, type: n.type, unit: n.unit ?? '', tags: n.tags.join(', '), favorite: n.favorite, content: n.content ?? '' }));
  }
  save() {
    const v = this.form.getRawValue(); const id = this.id();
    const body: NoteRequest = { title: v.title, content: v.content, languageId: v.languageId, type: v.type, unit: v.unit.trim() || null,
      tags: v.tags.split(',').map(t => t.trim()).filter(Boolean), favorite: v.favorite };
    (id ? this.api.updateNote(id, body) : this.api.createNote(body)).subscribe(n => { this.ui.toast('Note enregistrée'); this.router.navigate(['/notes', n.id]); });
  }
}
