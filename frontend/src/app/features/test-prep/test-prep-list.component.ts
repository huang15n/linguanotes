import { Component, inject, signal } from "@angular/core";
import { FormControl, FormGroup, ReactiveFormsModule } from "@angular/forms";
import { RouterLink } from "@angular/router";
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
import { ApiService } from "../../core/services/api.service";
import { LocaleService, TranslatePipe } from "../../core/services/locale.service";
import { UiService } from "../../shared/dialogs/dialogs";
import {
  EXAM_LANGUAGES,
  EXAM_SKILL_MODULES,
  EXAM_TYPES_BY_LANGUAGE,
  EXAM_LEVELS_BY_TYPE,
  ExamLanguage,
  ExamType,
  TestPreparation,
} from "../../core/models";

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
  styleUrl: "./test-prep-list.component.scss",
  templateUrl: "./test-prep-list.component.html",
})
export class TestPrepListComponent {
  private api = inject(ApiService);
  private ui = inject(UiService);
  private locale = inject(LocaleService);

  languages = EXAM_LANGUAGES;
  modules = EXAM_SKILL_MODULES;
  loading = signal(true);
  items = signal<TestPreparation[]>([]);

  private refresh$ = new BehaviorSubject<void>(undefined);
  form = new FormGroup({
    q: new FormControl(""),
    examLanguage: new FormControl<ExamLanguage | null>(null),
    examType: new FormControl<ExamType | null>(null),
    examLevel: new FormControl<string | null>(null),
    skillModule: new FormControl<string | null>(null),
    favorite: new FormControl(false),
  });

  typesForLang = signal<ExamType[]>([]);
  levelsForType = signal<string[]>([]);

  constructor() {
    this.form.controls.examLanguage.valueChanges.subscribe((lang) => {
      this.form.controls.examType.setValue(null, { emitEvent: false });
      this.form.controls.examLevel.setValue(null, { emitEvent: false });
      this.typesForLang.set(lang ? EXAM_TYPES_BY_LANGUAGE[lang] : []);
      this.levelsForType.set([]);
    });
    this.form.controls.examType.valueChanges.subscribe((type) => {
      this.form.controls.examLevel.setValue(null, { emitEvent: false });
      this.levelsForType.set(type ? EXAM_LEVELS_BY_TYPE[type] : []);
    });

    combineLatest([
      this.form.valueChanges.pipe(startWith(this.form.getRawValue()), debounceTime(250)),
      this.refresh$,
    ])
      .pipe(
        tap(() => this.loading.set(true)),
        switchMap(([v]) =>
          this.api
            .testPreparations({
              q: v.q || null,
              examLanguage: v.examLanguage || null,
              examType: v.examType || null,
              examLevel: v.examLevel || null,
              skillModule: (v.skillModule as any) || null,
              favorite: v.favorite || null,
            })
            .pipe(catchError(() => of([] as TestPreparation[]))),
        ),
      )
      .subscribe((list) => {
        this.items.set(list);
        this.loading.set(false);
      });
  }

  t(key: string) {
    return this.locale.translate(key);
  }

  moduleLabel(m: string) {
    return this.locale.translate(`testPrep.module.${m.toLowerCase()}`);
  }

  langLabel(l: string) {
    return this.locale.translate(`testPrep.lang.${l.toLowerCase()}`);
  }

  async remove(item: TestPreparation) {
    const ok = await this.ui.confirm(this.t("testPrep.deleteConfirm"));
    if (!ok) return;
    this.api.deleteTestPreparation(item.id).subscribe({
      next: () => this.refresh$.next(),
      error: () => this.ui.toast(this.t("common.error")),
    });
  }
}
