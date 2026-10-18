import { Component, OnInit, inject, signal } from "@angular/core";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { ApiService } from "../../core/services/api.service";
import { LocaleService, TranslatePipe } from "../../core/services/locale.service";
import { Language, VocabularyEntry } from "../../core/models";
import { UiService } from "../../shared/dialogs/dialogs";

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
  entries = signal<VocabularyEntry[]>([]);
  languages = signal<Language[]>([]);
  editingId = signal<number | null>(null);
  private editingLessonId = signal<number | null>(null);
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
    ipa: new FormControl("", { nonNullable: true }),
    partOfSpeech: new FormControl("", { nonNullable: true }),
    examples: new FormControl("", { nonNullable: true }),
    personalNotes: new FormControl("", { nonNullable: true }),
    difficulty: new FormControl(1, { nonNullable: true }),
    tags: new FormControl("", { nonNullable: true }),
  });

  ngOnInit() {
    this.api.languages().subscribe((items) => this.languages.set(items));
    this.load();
  }

  load() {
    this.api.vocabulary().subscribe((items) => this.entries.set(items));
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
      examples: value.examples
        .split("\n")
        .map((item) => item.trim())
        .filter(Boolean),
      personalNotes: value.personalNotes.trim() || null,
      difficulty: value.difficulty,
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
      this.cancelEdit();
      this.load();
    });
  }

  edit(entry: VocabularyEntry) {
    this.editingId.set(entry.id);
    this.editingLessonId.set(entry.lessonId);
    this.form.patchValue({
      sourceLanguageId: entry.sourceLanguageId,
      targetLanguageId: entry.targetLanguageId,
      sourceText: entry.sourceText,
      translation: entry.translation,
      ipa: entry.ipa ?? "",
      partOfSpeech: entry.partOfSpeech ?? "",
      examples: entry.examples.join("\n"),
      personalNotes: entry.personalNotes ?? "",
      difficulty: entry.difficulty,
      tags: entry.tags.join(", "),
    });
  }

  cancelEdit() {
    this.editingId.set(null);
    this.editingLessonId.set(null);
    this.form.reset({
      sourceLanguageId: null,
      targetLanguageId: null,
      sourceText: "",
      translation: "",
      ipa: "",
      partOfSpeech: "",
      examples: "",
      personalNotes: "",
      difficulty: 1,
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
}
