import { Component, OnInit, computed, inject, signal, viewChild } from "@angular/core";
import { Observable, catchError, forkJoin, of, tap } from "rxjs";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { ElementRef } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { ApiService } from "../../core/services/api.service";
import {
  LocaleService,
  TranslatePipe,
} from "../../core/services/locale.service";
import {
  ConjugationTable,
  Language,
  LanguageLevel,
  LearningStatus,
  VocabularyEntry,
} from "../../core/models";
import { UiService } from "../../shared/dialogs/dialogs";

const TYPES = [
  "NOUN",
  "VERB",
  "ADJECTIVE",
  "ADVERB",
  "PREPOSITION",
  "EXPRESSION",
  "CONJUNCTION",
  "PRONOUN",
  "DETERMINER",
  "OTHER",
];

@Component({
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    TranslatePipe,
  ],
  styleUrl: "./vocabulary.component.scss",
  templateUrl: "./vocabulary.component.html",
})
export class VocabularyComponent implements OnInit {
  private api = inject(ApiService);
  private locale = inject(LocaleService);
  private ui = inject(UiService);
  readonly importInput = viewChild<ElementRef<HTMLInputElement>>("importInput");
  readonly excelInput = viewChild<ElementRef<HTMLInputElement>>("excelInput");
  entries = signal<VocabularyEntry[]>([]);
  languages = signal<Language[]>([]);
  editingId = signal<number | null>(null);
  private editingLessonId = signal<number | null>(null);
  conjugations = signal<ConjugationTable[]>([]);
  query = signal("");
  languageFilter = signal<number | null>(null);
  levelFilter = signal("");
  statusFilter = signal<LearningStatus | "">("");
  typeFilter = signal("");
  importing = signal(false);
  importReport = signal("");
  types = TYPES;
  statuses: LearningStatus[] = ["NEW", "LEARNING", "MASTERED", "TO_REVIEW"];
  form = new FormGroup({
    sourceLanguageId: new FormControl<number | null>(null, Validators.required),
    targetLanguageId: new FormControl<number | null>(null, Validators.required),
    sourceText: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(500)],
    }),
    translation: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(1000)],
    }),
    partOfSpeech: new FormControl("", { nonNullable: true }),
    grammaticalGender: new FormControl("", { nonNullable: true }),
    level: new FormControl("", { nonNullable: true }),
    definition: new FormControl("", { nonNullable: true }),
    ipa: new FormControl("", { nonNullable: true }),
    examples: new FormControl("", { nonNullable: true }),
    synonyms: new FormControl("", { nonNullable: true }),
    antonyms: new FormControl("", { nonNullable: true }),
    personalNotes: new FormControl("", { nonNullable: true }),
    region: new FormControl("", { nonNullable: true }),
    pluralForm: new FormControl("", { nonNullable: true }),
    difficulty: new FormControl(1, { nonNullable: true }),
    learningStatus: new FormControl<LearningStatus>("NEW", { nonNullable: true }),
    tags: new FormControl("", { nonNullable: true }),
  });

  filtered = computed(() => {
    const query = this.query().trim().toLowerCase();
    return this.entries().filter((entry) => {
      if (
        query &&
        !`${entry.sourceText} ${entry.translation} ${entry.definition ?? ""}`
          .toLowerCase()
          .includes(query)
      )
        return false;
      if (this.languageFilter() && entry.targetLanguageId !== this.languageFilter())
        return false;
      if (this.levelFilter() && entry.level !== this.levelFilter()) return false;
      if (this.statusFilter() && entry.learningStatus !== this.statusFilter())
        return false;
      if (
        this.typeFilter() &&
        (entry.partOfSpeech ?? "").toUpperCase() !== this.typeFilter()
      )
        return false;
      return true;
    });
  });
  isVerb = computed(
    () => this.form.controls.partOfSpeech.value.toUpperCase() === "VERB",
  );
  isNoun = computed(
    () => this.form.controls.partOfSpeech.value.toUpperCase() === "NOUN",
  );

  ngOnInit() {
    this.api.languages().subscribe((items) => this.languages.set(items));
    this.load();
  }

  load() {
    this.api.vocabulary().subscribe((items) => this.entries.set(items));
  }

  targetLevels() {
    const language = this.languages().find(
      (item) => item.id === this.form.controls.targetLanguageId.value,
    );
    return language?.levels ?? [];
  }

  filterLevels(): LanguageLevel[] {
    const languageId = this.languageFilter();
    const languages = languageId
      ? this.languages().filter((language) => language.id === languageId)
      : this.languages();
    const levels = new Map<string, LanguageLevel>();
    for (const language of languages) {
      for (const level of language.levels) {
        if (!levels.has(level.code)) levels.set(level.code, level);
      }
    }
    return [...levels.values()].sort((a, b) => a.position - b.position);
  }

  save() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    if (value.sourceLanguageId === null || value.targetLanguageId === null)
      return;
    const body = {
      sourceLanguageId: value.sourceLanguageId,
      targetLanguageId: value.targetLanguageId,
      sourceText: value.sourceText.trim(),
      translation: value.translation.trim(),
      ipa: value.ipa.trim() || null,
      partOfSpeech: value.partOfSpeech.trim() || null,
      grammaticalGender: value.grammaticalGender.trim() || null,
      level: value.level.trim() || null,
      definition: value.definition.trim() || null,
      examples: this.lines(value.examples),
      synonyms: this.lines(value.synonyms),
      antonyms: this.lines(value.antonyms),
      personalNotes: value.personalNotes.trim() || null,
      difficulty: value.difficulty,
      learningStatus: value.learningStatus,
      region: value.region.trim() || null,
      pluralForm: value.pluralForm.trim() || null,
      conjugations: this.conjugations(),
      tags: value.tags
        .split(",")
        .map((item) => item.trim())
        .filter(Boolean),
      lessonId: this.editingLessonId(),
    };
    const id = this.editingId();
    const request = id
      ? this.api.updateVocabulary(id, body)
      : this.api.createVocabulary(body);
    request.subscribe(() => {
      this.ui.toast(this.locale.translate("vocabulary.saved"));
      this.cancelEdit();
      this.load();
    });
  }

  edit(entry: VocabularyEntry) {
    this.editingId.set(entry.id);
    this.editingLessonId.set(entry.lessonId);
    this.conjugations.set(entry.conjugations ?? []);
    this.form.patchValue({
      sourceLanguageId: entry.sourceLanguageId,
      targetLanguageId: entry.targetLanguageId,
      sourceText: entry.sourceText,
      translation: entry.translation,
      partOfSpeech: entry.partOfSpeech ?? "",
      grammaticalGender: entry.grammaticalGender ?? "",
      level: entry.level ?? "",
      definition: entry.definition ?? "",
      ipa: entry.ipa ?? "",
      examples: entry.examples.join("\n"),
      synonyms: entry.synonyms.join("\n"),
      antonyms: entry.antonyms.join("\n"),
      personalNotes: entry.personalNotes ?? "",
      region: entry.region ?? "",
      pluralForm: entry.pluralForm ?? "",
      difficulty: entry.difficulty,
      learningStatus: entry.learningStatus ?? "NEW",
      tags: entry.tags.join(", "),
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  cancelEdit() {
    this.editingId.set(null);
    this.editingLessonId.set(null);
    this.conjugations.set([]);
    this.form.reset({
      sourceLanguageId: null,
      targetLanguageId: null,
      sourceText: "",
      translation: "",
      partOfSpeech: "",
      grammaticalGender: "",
      level: "",
      definition: "",
      ipa: "",
      examples: "",
      synonyms: "",
      antonyms: "",
      personalNotes: "",
      region: "",
      pluralForm: "",
      difficulty: 1,
      learningStatus: "NEW",
      tags: "",
    });
  }

  remove(entry: VocabularyEntry) {
    this.ui
      .confirm(this.locale.translate("vocabulary.deleteConfirm"))
      .subscribe(() =>
        this.api.deleteVocabulary(entry.id).subscribe(() => this.load()),
      );
  }

  addTable() {
    this.conjugations.update((tables) => [
      ...tables,
      {
        title: "",
        mood: "Indicatif",
        tense: "Présent",
        variant: "",
        note: "",
        rows: [{ person: "", form: "", note: "" }],
      },
    ]);
  }

  removeTable(index: number) {
    this.conjugations.update((tables) =>
      tables.filter((_, itemIndex) => itemIndex !== index),
    );
  }

  addRow(tableIndex: number) {
    this.conjugations.update((tables) =>
      tables.map((table, index) =>
        index === tableIndex
          ? {
              ...table,
              rows: [...table.rows, { person: "", form: "", note: "" }],
            }
          : table,
      ),
    );
  }

  removeRow(tableIndex: number, rowIndex: number) {
    this.conjugations.update((tables) =>
      tables.map((table, index) =>
        index === tableIndex
          ? {
              ...table,
              rows: table.rows.filter((_, itemIndex) => itemIndex !== rowIndex),
            }
          : table,
      ),
    );
  }

  setTable(
    tableIndex: number,
    field: "title" | "mood" | "tense" | "variant" | "note",
    value: string,
  ) {
    this.conjugations.update((tables) =>
      tables.map((table, index) =>
        index === tableIndex ? { ...table, [field]: value } : table,
      ),
    );
  }

  setRow(
    tableIndex: number,
    rowIndex: number,
    field: "person" | "form" | "note",
    value: string,
  ) {
    this.conjugations.update((tables) =>
      tables.map((table, index) =>
        index === tableIndex
          ? {
              ...table,
              rows: table.rows.map((row, itemIndex) =>
                itemIndex === rowIndex ? { ...row, [field]: value } : row,
              ),
            }
          : table,
      ),
    );
  }

  exportCsv() {
    const header = [
      "id",
      "sourceLanguageId",
      "sourceLanguageName",
      "targetLanguageId",
      "targetLanguageName",
      "sourceText",
      "translation",
      "partOfSpeech",
      "grammaticalGender",
      "level",
      "definition",
      "ipa",
      "examples",
      "synonyms",
      "antonyms",
      "personalNotes",
      "difficulty",
      "learningStatus",
      "region",
      "pluralForm",
      "conjugations",
      "tags",
    ];
    const rows = this.filtered().map((entry) =>
      [
        entry.id,
        entry.sourceLanguageId,
        entry.sourceLanguageName,
        entry.targetLanguageId,
        entry.targetLanguageName,
        entry.sourceText,
        entry.translation,
        entry.partOfSpeech ?? "",
        entry.grammaticalGender ?? "",
        entry.level ?? "",
        entry.definition ?? "",
        entry.ipa ?? "",
        entry.examples.join(" | "),
        entry.synonyms.join(" | "),
        entry.antonyms.join(" | "),
        entry.personalNotes ?? "",
        entry.difficulty,
        entry.learningStatus,
        entry.region ?? "",
        entry.pluralForm ?? "",
        JSON.stringify(entry.conjugations ?? []),
        entry.tags.join(" | "),
      ]
        .map((value) => this.csvCell(String(value)))
        .join(","),
    );
    const content = [header.join(","), ...rows].join("\r\n");
    const blob = new Blob(["\ufeff" + content], {
      type: "text/csv;charset=utf-8",
    });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = "linguanotes-vocabulaire.csv";
    link.click();
    URL.revokeObjectURL(url);
  }

  openImport() {
    this.importInput()?.nativeElement.click();
  }

  openExcelImport() {
    this.excelInput()?.nativeElement.click();
  }

  exportExcel() {
    this.api.exportVocabularyExcel().subscribe((blob) => {
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = "linguanotes-vocabulaire.xlsx";
      link.click();
      URL.revokeObjectURL(url);
    });
  }

  importExcel(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.item(0);
    if (!file) return;
    this.importing.set(true);
    this.importReport.set("");
    this.api.importVocabularyExcel(file).subscribe({
      next: (report) => {
        this.importing.set(false);
        this.importReport.set(
          `${report.imported} importés, ${report.failed} en erreur` +
            (report.errors.length ? ` · ${report.errors[0]}` : ""),
        );
        this.load();
        input.value = "";
      },
      error: () => {
        this.importing.set(false);
        input.value = "";
      },
    });
  }

  importCsv(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.item(0);
    if (!file) return;
    this.importing.set(true);
    this.importReport.set("");
    file.text().then((text) => {
      const rows = this.parseCsv(text.replace(/^\ufeff/, ""));
      if (rows.length < 2) {
        this.importing.set(false);
        this.importReport.set(this.locale.translate("vocabulary.importEmpty"));
        return;
      }
      const header = rows[0].map((cell) => cell.trim());
      const index = (name: string) => header.indexOf(name);
      const sourceIdIndex = index("sourceLanguageId");
      const targetIdIndex = index("targetLanguageId");
      const sourceTextIndex = index("sourceText");
      const translationIndex = index("translation");
      if (sourceIdIndex < 0 || targetIdIndex < 0 || sourceTextIndex < 0 || translationIndex < 0) {
        this.importing.set(false);
        this.importReport.set(this.locale.translate("vocabulary.importHeader"));
        return;
      }
      let imported = 0;
      let failed = 0;
      const calls: Observable<unknown>[] = [];
      for (const row of rows.slice(1).filter((row) => row.some((cell) => cell.trim()))) {
        const sourceLanguageId = Number(row[sourceIdIndex]);
        const targetLanguageId = Number(row[targetIdIndex]);
        if (!sourceLanguageId || !targetLanguageId) {
          failed++;
          continue;
        }
        const get = (name: string) => {
          const position = index(name);
          return position < 0 ? "" : (row[position] ?? "").trim();
        };
        calls.push(
          this.api.createVocabulary({
            sourceLanguageId,
            targetLanguageId,
            sourceText: get("sourceText"),
            translation: get("translation"),
            partOfSpeech: get("partOfSpeech") || null,
            grammaticalGender: get("grammaticalGender") || null,
            level: get("level") || null,
            definition: get("definition") || null,
            ipa: get("ipa") || null,
            examples: this.splitPipes(get("examples")),
            synonyms: this.splitPipes(get("synonyms")),
            antonyms: this.splitPipes(get("antonyms")),
            personalNotes: get("personalNotes") || null,
            difficulty: Number(get("difficulty")) || 1,
            learningStatus: get("learningStatus") || "NEW",
            region: get("region") || null,
            pluralForm: get("pluralForm") || null,
            conjugations: this.safeConjugations(get("conjugations")),
            tags: this.splitPipes(get("tags")),
          }),
        );
      }
      if (!calls.length) {
        this.importing.set(false);
        this.importReport.set(`${imported} importés, ${failed} en erreur`);
        return;
      }
      forkJoin(
        calls.map((call) =>
          call.pipe(
            tap(() => imported++),
            catchError(() => {
              failed++;
              return of(null);
            }),
          ),
        ),
      ).subscribe(() => {
        this.importing.set(false);
        this.importReport.set(`${imported} importés, ${failed} en erreur`);
        this.load();
        input.value = "";
      });
    });
  }

  private lines(value: string) {
    return value
      .split("\n")
      .map((item) => item.trim())
      .filter(Boolean);
  }

  private splitPipes(value: string) {
    return value
      .split("|")
      .map((item) => item.trim())
      .filter(Boolean);
  }

  private safeConjugations(value: string): ConjugationTable[] {
    if (!value) return [];
    try {
      const parsed = JSON.parse(value);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }

  private csvCell(value: string) {
    return `"${value.replace(/"/g, '""')}"`;
  }

  private parseCsv(text: string): string[][] {
    const rows: string[][] = [];
    let row: string[] = [];
    let cell = "";
    let quoted = false;
    for (let index = 0; index < text.length; index++) {
      const char = text[index];
      if (quoted) {
        if (char === '"' && text[index + 1] === '"') {
          cell += '"';
          index++;
        } else if (char === '"') {
          quoted = false;
        } else {
          cell += char;
        }
      } else if (char === '"') {
        quoted = true;
      } else if (char === ",") {
        row.push(cell);
        cell = "";
      } else if (char === "\n") {
        row.push(cell);
        rows.push(row);
        row = [];
        cell = "";
      } else if (char !== "\r") {
        cell += char;
      }
    }
    if (cell.length || row.length) {
      row.push(cell);
      rows.push(row);
    }
    return rows;
  }
}
