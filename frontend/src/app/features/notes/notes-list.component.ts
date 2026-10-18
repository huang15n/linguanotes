import { Component, inject, signal } from "@angular/core";
import { FormControl, FormGroup, ReactiveFormsModule } from "@angular/forms";
import { RouterLink } from "@angular/router";
import { toSignal } from "@angular/core/rxjs-interop";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatChipsModule } from "@angular/material/chips";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatSlideToggleModule } from "@angular/material/slide-toggle";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import {
  BehaviorSubject,
  catchError,
  combineLatest,
  debounceTime,
  of,
  startWith,
  switchMap,
  tap,
} from "rxjs";
import { ApiService } from "../../core/api.service";
import { LocaleService, TranslatePipe } from "../../core/locale.service";
import { UiService } from "../../shared/dialogs/dialogs";
import { Note, NoteFilters, NOTE_TYPES } from "../../models";

@Component({
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatProgressBarModule,
    TranslatePipe,
  ],
  styleUrl: "./notes-list.component.scss",
  templateUrl: "./notes-list.component.html",
})
export class NotesListComponent {
  private api = inject(ApiService);
  private ui = inject(UiService);
  private locale = inject(LocaleService);
  types = NOTE_TYPES;
  tl = (type: string) => this.locale.translate(`type.${type}`);
  loading = signal(true);
  languages = toSignal(this.api.languages(), { initialValue: [] });
  tags = toSignal(this.api.tags(), { initialValue: [] });
  private refresh$ = new BehaviorSubject<void>(undefined);
  form = new FormGroup({
    q: new FormControl(""),
    languageId: new FormControl<number | null>(null),
    type: new FormControl<string | null>(null),
    unit: new FormControl(""),
    tag: new FormControl<string | null>(null),
    favorite: new FormControl(false),
  });
  notes = toSignal(
    combineLatest([
      this.form.valueChanges.pipe(
        startWith(this.form.value),
        debounceTime(250),
      ),
      this.refresh$,
    ]).pipe(
      tap(() => this.loading.set(true)),
      switchMap(([f]) =>
        this.api
          .notes(f as NoteFilters)
          .pipe(catchError(() => of([] as Note[]))),
      ),
      tap(() => this.loading.set(false)),
    ),
    { initialValue: [] as Note[] },
  );
  excerpt(md: string | null) {
    return (md ?? "")
      .replace(/[#>*_`~|\[\]]|^-{3,}$|^[-+] /gm, "")
      .replace(/\s+/g, " ")
      .trim()
      .slice(0, 160);
  }
  fav(n: Note) {
    this.api.toggleFavorite(n.id).subscribe(() => this.refresh$.next());
  }
  remove(n: Note) {
    this.ui.confirm(this.locale.translate("note.deleteConfirm")).subscribe(() =>
      this.api.deleteNote(n.id).subscribe(() => {
        this.ui.toast(this.locale.translate("note.deleted"));
        this.refresh$.next();
      }),
    );
  }
}
