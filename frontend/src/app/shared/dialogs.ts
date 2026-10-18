import { Component, Injectable, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { filter } from 'rxjs';

export interface Field { key: string; label: string; }
export interface FormDialogData { title: string; fields: Field[]; value?: Record<string, string>; submit?: string; }

@Component({
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  template: `<h2 mat-dialog-title>{{ data.title }}</h2>
  <mat-dialog-content><form [formGroup]="form" class="col">
    @for (f of data.fields; track f.key) { <mat-form-field appearance="outline"><mat-label>{{ f.label }}</mat-label><input matInput [formControlName]="f.key"></mat-form-field> }
  </form></mat-dialog-content>
  <mat-dialog-actions align="end"><button mat-button mat-dialog-close>Annuler</button>
    <button mat-flat-button [disabled]="form.invalid" [mat-dialog-close]="form.value">{{ data.submit ?? 'Enregistrer' }}</button></mat-dialog-actions>`
})
export class FormDialogComponent {
  data = inject<FormDialogData>(MAT_DIALOG_DATA);
  form = new FormGroup(Object.fromEntries(this.data.fields.map(f => [f.key, new FormControl(this.data.value?.[f.key] ?? '', { nonNullable: true, validators: Validators.required })])));
}

@Component({
  imports: [MatDialogModule, MatButtonModule],
  template: `<h2 mat-dialog-title>Confirmation</h2><mat-dialog-content>{{ msg }}</mat-dialog-content>
  <mat-dialog-actions align="end"><button mat-button mat-dialog-close>Annuler</button><button mat-flat-button [mat-dialog-close]="true">Confirmer</button></mat-dialog-actions>`
})
export class ConfirmDialogComponent { msg = inject<string>(MAT_DIALOG_DATA); }

@Injectable({ providedIn: 'root' })
export class UiService {
  private d = inject(MatDialog); private sb = inject(MatSnackBar);
  form(data: FormDialogData) { return this.d.open(FormDialogComponent, { data, width: '480px', maxWidth: '95vw' }).afterClosed().pipe(filter(Boolean)); }
  confirm(msg: string) { return this.d.open(ConfirmDialogComponent, { data: msg }).afterClosed().pipe(filter(v => v === true)); }
  toast(m: string) { this.sb.open(m, 'OK', { duration: 3000 }); }
}
