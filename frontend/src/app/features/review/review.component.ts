import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { RouterLink } from "@angular/router";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatCheckboxModule } from "@angular/material/checkbox";
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
  DueCard,
  Language,
  LanguageLevel,
  LearningStatus,
  ReviewMode,
  ReviewSession,
  VocabularyEntry,
} from "../../core/models";
import { UiService } from "../../shared/dialogs/dialogs";

type Tab = "cards" | "selection";

@Component({
  imports: [
    RouterLink,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    TranslatePipe,
  ],
  styleUrl: "./review.component.scss",
  templateUrl: "./review.component.html",
})
export class ReviewComponent implements OnInit {
  private api = inject(ApiService);
  private locale = inject(LocaleService);
  private ui = inject(UiService);
  tab = signal<Tab>("selection");
  due = signal<DueCard[]>([]);
  cards = signal<DueCard[]>([]);
  current = signal<DueCard | null>(null);
  revealed = signal(false);
  form = new FormGroup({
    frontText: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required],
    }),
    backText: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  vocabulary = signal<VocabularyEntry[]>([]);
  languages = signal<Language[]>([]);
  languageFilter = signal<number | null>(null);
  levelFilter = signal<string[]>([]);
  statusFilter = signal<LearningStatus | "">("");
  selectedIds = signal<Set<number>>(new Set());
  mode = signal<ReviewMode>("FLASHCARD");
  sessionEntries = signal<VocabularyEntry[]>([]);
  sessionIndex = signal(0);
  sessionCorrect = signal(0);
  sessionIncorrect = signal(0);
  sessionDone = signal(false);
  sessionRevealed = signal(false);
  feedback = signal<"correct" | "wrong" | null>(null);
  choices = signal<string[]>([]);
  results = signal<boolean[]>([]);
  lastSession = signal<ReviewSession | null>(null);
  saving = signal(false);
  answer = new FormControl("", { nonNullable: true });

  filteredVocabulary = computed(() => {
    const levels = this.levelFilter();
    return this.vocabulary().filter((entry) => {
      if (this.languageFilter() && entry.targetLanguageId !== this.languageFilter())
        return false;
      if (levels.length && (!entry.level || !levels.includes(entry.level)))
        return false;
      if (this.statusFilter() && entry.learningStatus !== this.statusFilter())
        return false;
      return true;
    });
  });
  selectedCount = computed(() => this.selectedIds().size);
  allVisibleSelected = computed(
    () =>
      this.filteredVocabulary().length > 0 &&
      this.filteredVocabulary().every((entry) =>
        this.selectedIds().has(entry.id),
      ),
  );
  currentEntry = computed(
    () => this.sessionEntries()[this.sessionIndex()] ?? null,
  );
  progress = computed(() => {
    const total = this.sessionEntries().length;
    return total ? ((this.sessionIndex() + 1) / total) * 100 : 0;
  });
  score = computed(() => {
    const total = this.sessionCorrect() + this.sessionIncorrect();
    return total ? Math.round((this.sessionCorrect() / total) * 100) : 0;
  });

  ngOnInit() {
    this.api.languages().subscribe((items) => this.languages.set(items));
    this.api.vocabulary().subscribe((items) => this.vocabulary.set(items));
    this.loadCards();
  }

  setTab(tab: Tab) {
    this.tab.set(tab);
    if (tab === "cards") this.loadCards();
  }

  loadCards() {
    this.api.dueCards().subscribe((items) => {
      this.due.set(items);
      this.current.set(items[0] ?? null);
      this.revealed.set(false);
    });
    this.api.flashcards().subscribe((items) =>
      this.cards.set(
        items.map((item) => ({
          flashcardId: item.id,
          lessonId: item.lessonId,
          frontText: item.frontText,
          backText: item.backText,
          repetitions: 0,
          correctCount: 0,
          incorrectCount: 0,
          nextDueAt: "",
        })),
      ),
    );
  }

  reveal() {
    this.revealed.set(true);
  }

  answerDue(rating: "AGAIN" | "HARD" | "GOOD" | "EASY") {
    const current = this.current();
    if (!current) return;
    this.api.answerCard(current.flashcardId, rating).subscribe(() => this.loadCards());
  }

  createCard() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    this.api
      .createFlashcard({
        lessonId: null,
        vocabularyEntryId: null,
        frontText: value.frontText.trim(),
        backText: value.backText.trim(),
      })
      .subscribe(() => {
        this.form.reset({ frontText: "", backText: "" });
        this.loadCards();
      });
  }

  removeCard(card: DueCard) {
    this.ui
      .confirm(
        this.locale
          .translate("common.deleteConfirm")
          .replace("{name}", card.frontText),
      )
      .subscribe(() =>
        this.api.deleteFlashcard(card.flashcardId).subscribe(() => {
          this.ui.toast(this.locale.translate("common.deleted"));
          this.loadCards();
        }),
      );
  }

  filterLevels(): LanguageLevel[] {
    const language = this.languages().find(
      (item) => item.id === this.languageFilter(),
    );
    const sources = language ? [language] : this.languages();
    const levels = new Map<string, LanguageLevel>();
    for (const source of sources) {
      for (const level of source.levels) {
        if (!levels.has(level.code)) levels.set(level.code, level);
      }
    }
    return [...levels.values()].sort((a, b) => a.position - b.position);
  }

  toggle(entry: VocabularyEntry) {
    const selected = new Set(this.selectedIds());
    if (selected.has(entry.id)) selected.delete(entry.id);
    else selected.add(entry.id);
    this.selectedIds.set(selected);
  }

  selectAll() {
    const selected = new Set(this.selectedIds());
    for (const entry of this.filteredVocabulary()) selected.add(entry.id);
    this.selectedIds.set(selected);
  }

  clearSelection() {
    this.selectedIds.set(new Set());
  }

  start() {
    const entries = this.vocabulary().filter((entry) =>
      this.selectedIds().has(entry.id),
    );
    if (!entries.length) return;
    this.sessionEntries.set(this.shuffle(entries));
    this.sessionIndex.set(0);
    this.sessionCorrect.set(0);
    this.sessionIncorrect.set(0);
    this.results.set([]);
    this.sessionDone.set(false);
    this.sessionRevealed.set(false);
    this.feedback.set(null);
    this.answer.reset("");
    this.lastSession.set(null);
    this.buildChoices();
  }

  prompt() {
    const entry = this.currentEntry();
    if (!entry) return "";
    return this.mode() === "SOURCE_TO_TARGET"
      ? entry.translation
      : entry.sourceText;
  }

  expected() {
    const entry = this.currentEntry();
    if (!entry) return "";
    return this.mode() === "SOURCE_TO_TARGET"
      ? entry.sourceText
      : entry.translation;
  }

  checkText() {
    if (!this.answer.value.trim()) return;
    this.record(this.normalize(this.answer.value) === this.normalize(this.expected()));
  }

  choose(choice: string) {
    this.record(this.normalize(choice) === this.normalize(this.expected()));
  }

  revealSession() {
    this.sessionRevealed.set(true);
  }

  mark(correct: boolean) {
    this.record(correct);
  }

  next() {
    if (this.sessionIndex() + 1 >= this.sessionEntries().length) {
      this.finish();
      return;
    }
    this.sessionIndex.update((index) => index + 1);
    this.sessionRevealed.set(false);
    this.feedback.set(null);
    this.answer.reset("");
    this.choices.set([]);
    this.buildChoices();
  }

  restart() {
    this.start();
  }

  newSelection() {
    this.sessionDone.set(false);
    this.sessionEntries.set([]);
    this.results.set([]);
    this.tab.set("selection");
  }

  private record(correct: boolean) {
    this.results.update((items) => [...items, correct]);
    if (correct) this.sessionCorrect.update((value) => value + 1);
    else this.sessionIncorrect.update((value) => value + 1);
    this.feedback.set(correct ? "correct" : "wrong");
    if (this.mode() === "FLASHCARD") {
      setTimeout(() => this.next());
    }
  }

  private finish() {
    this.sessionDone.set(true);
    const items = this.sessionEntries().map((entry, index) => ({
      vocabularyEntryId: entry.id,
      correct: this.results()[index] ?? false,
    }));
    this.saving.set(true);
    this.api
      .recordReviewSession({
        languageId: this.languageFilter(),
        mode: this.mode(),
        items,
      })
      .subscribe({
        next: (session) => this.lastSession.set(session),
        complete: () => this.saving.set(false),
        error: () => this.saving.set(false),
      });
  }

  private buildChoices() {
    if (this.mode() !== "CHOICE") return;
    const entry = this.currentEntry();
    if (!entry) return;
    const others = this.shuffle(
      this.vocabulary()
        .filter((item) => item.id !== entry.id)
        .map((item) => item.translation)
        .filter((value) => value !== entry.translation),
    ).slice(0, 3);
    this.choices.set(this.shuffle([entry.translation, ...others]));
  }

  private normalize(value: string) {
    return value
      .trim()
      .toLowerCase()
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .replace(/\s+/g, " ");
  }

  private shuffle<T>(items: T[]): T[] {
    const copy = [...items];
    for (let index = copy.length - 1; index > 0; index--) {
      const target = Math.floor(Math.random() * (index + 1));
      [copy[index], copy[target]] = [copy[target], copy[index]];
    }
    return copy;
  }
}
