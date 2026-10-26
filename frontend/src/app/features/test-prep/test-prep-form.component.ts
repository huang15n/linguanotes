import { Component, OnInit, inject, signal } from "@angular/core";
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { MatButtonModule } from "@angular/material/button";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatSlideToggleModule } from "@angular/material/slide-toggle";
import { MatIconModule } from "@angular/material/icon";
import { ApiService } from "../../core/services/api.service";
import { LocaleService, TranslatePipe } from "../../core/services/locale.service";
import { UiService } from "../../shared/dialogs/dialogs";
import { MarkdownEditorComponent } from "../../shared/markdown/markdown-editor.component";
import {
  EXAM_LANGUAGES,
  EXAM_LEVELS_BY_TYPE,
  EXAM_SKILL_MODULES,
  EXAM_TYPES_BY_LANGUAGE,
  ExamLanguage,
  ExamType,
  TestPreparationRequest,
} from "../../core/models";

@Component({
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatIconModule,
    TranslatePipe,
    MarkdownEditorComponent,
  ],
  styleUrl: "./test-prep-form.component.scss",
  templateUrl: "./test-prep-form.component.html",
})
export class TestPrepFormComponent implements OnInit {
  /** Tableaux adaptés à la préparation d'examen (pas le modèle vocabulaire des notes). */
  examTableTemplate =
    "\n| N° | Consignes / Question | Réponse attendue | Points | Notes |\n| --- | --- | --- | --- | --- |\n| 1 |  |  |  |  |\n";

  private api = inject(ApiService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private ui = inject(UiService);
  private locale = inject(LocaleService);

  languages = EXAM_LANGUAGES;
  modules = EXAM_SKILL_MODULES;
  typesForLang = signal<ExamType[]>([]);
  levelsForType = signal<string[]>([]);
  editId = signal<number | null>(null);
  saving = signal(false);

  form = new FormGroup({
    title: new FormControl("", { nonNullable: true, validators: [Validators.required, Validators.maxLength(255)] }),
    content: new FormControl("", { nonNullable: true }),
    examLanguage: new FormControl<ExamLanguage | null>(null, Validators.required),
    examType: new FormControl<ExamType | null>(null, Validators.required),
    examLevel: new FormControl<string | null>(null, Validators.required),
    skillModule: new FormControl<string | null>(null, Validators.required),
    visibility: new FormControl<"PRIVATE" | "PUBLIC">("PRIVATE", { nonNullable: true }),
    favorite: new FormControl(false, { nonNullable: true }),
  });

  ngOnInit() {
    this.form.controls.examLanguage.valueChanges.subscribe((lang) => {
      const types = lang ? EXAM_TYPES_BY_LANGUAGE[lang] : [];
      this.typesForLang.set(types);
      const current = this.form.controls.examType.value;
      if (current && !types.includes(current)) {
        this.form.controls.examType.setValue(null);
        this.form.controls.examLevel.setValue(null);
        this.levelsForType.set([]);
      }
    });
    this.form.controls.examType.valueChanges.subscribe((type) => {
      const levels = type ? EXAM_LEVELS_BY_TYPE[type] : [];
      this.levelsForType.set(levels);
      const current = this.form.controls.examLevel.value;
      if (current && !levels.includes(current)) {
        this.form.controls.examLevel.setValue(null);
      }
    });

    const idParam = this.route.snapshot.paramMap.get("id");
    if (idParam && idParam !== "new") {
      const id = Number(idParam);
      this.editId.set(id);
      this.api.testPreparation(id).subscribe({
        next: (item) => {
          this.typesForLang.set(EXAM_TYPES_BY_LANGUAGE[item.examLanguage]);
          this.levelsForType.set(EXAM_LEVELS_BY_TYPE[item.examType]);
          this.form.patchValue({
            title: item.title,
            content: item.content ?? "",
            examLanguage: item.examLanguage,
            examType: item.examType,
            examLevel: item.examLevel,
            skillModule: item.skillModule,
            visibility: item.visibility === "PUBLIC" ? "PUBLIC" : "PRIVATE",
            favorite: item.favorite,
          });
        },
        error: () => this.ui.toast(this.locale.translate("common.error")),
      });
    }
  }

  save() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const body: TestPreparationRequest = {
      title: v.title.trim(),
      content: v.content,
      examLanguage: v.examLanguage!,
      examType: v.examType!,
      examLevel: v.examLevel!,
      skillModule: v.skillModule as any,
      visibility: v.visibility,
      favorite: v.favorite,
      audioFileId: null,
    };
    this.saving.set(true);
    const id = this.editId();
    const req$ = id
      ? this.api.updateTestPreparation(id, body)
      : this.api.createTestPreparation(body);
    req$.subscribe({
      next: (saved) => {
        this.saving.set(false);
        this.router.navigate(["/test-prep", saved.id]);
      },
      error: () => {
        this.saving.set(false);
        this.ui.toast(this.locale.translate("common.error"));
      },
    });
  }
}
