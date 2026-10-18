import {
  Component,
  DestroyRef,
  HostListener,
  OnDestroy,
  OnInit,
  inject,
  input,
  signal,
} from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { DatePipe } from "@angular/common";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { Router, RouterLink } from "@angular/router";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatIconModule } from "@angular/material/icon";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatSlideToggleModule } from "@angular/material/slide-toggle";
import { MatTooltipModule } from "@angular/material/tooltip";
import { catchError, debounceTime, of, switchMap, tap } from "rxjs";
import { MarkdownEditorComponent } from "../../shared/markdown/markdown-editor.component";
import { ApiService } from "../../core/services/api.service";
import {
  LocaleService,
  TranslatePipe,
} from "../../core/services/locale.service";
import { UiService } from "../../shared/dialogs/dialogs";
import {
  Language,
  LanguageLevel,
  NOTE_TYPES,
  NoteRequest,
  NoteType,
  NoteVisibility,
  SchoolClass,
  Tag,
} from "../../core/models";

type SaveState = "saved" | "saving" | "unsaved" | "draft" | "conflict" | "error";

const DEFAULT_LEVELS: LanguageLevel[] = ["A1", "A2", "B1", "B2", "C1", "C2"].map(
  (code, index) => ({ id: index, code, label: code, position: index }),
);

@Component({
  imports: [
    MarkdownEditorComponent,
    TranslatePipe,
    ReactiveFormsModule,
    RouterLink,
    DatePipe,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatTooltipModule,
  ],
  styleUrl: "./note-form.component.scss",
  templateUrl: "./note-form.component.html",
})
export class NoteFormComponent implements OnInit, OnDestroy {
  id = input<string>();
  private api = inject(ApiService);
  locale = inject(LocaleService);
  private ui = inject(UiService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);
  private ready = false;
  languages = signal<Language[]>([]);
  classes = signal<SchoolClass[]>([]);
  tagCatalog = signal<Tag[]>([]);
  lessonOptions = signal<{ id: number; label: string }[]>([]);
  saveState = signal<SaveState>("saved");
  lastSavedAt = signal<string | null>(null);
  draftRestored = signal(false);
  conflict = signal(false);
  private version = signal(0);
  private dirty = false;
  types = NOTE_TYPES;
  visibilityOptions: { value: NoteVisibility; label: string }[] = [
    { value: "PRIVATE", label: "note.visibilityPrivate" },
    { value: "SHARED", label: "note.visibilityShared" },
    { value: "CLASS", label: "note.visibilityClass" },
    { value: "PUBLIC", label: "note.visibilityPublic" },
  ];
  tl = (type: string) => this.locale.translate(`type.${type}`);
  form = new FormGroup({
    title: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(255)],
    }),
    languageId: new FormControl<number | null>(null),
    level: new FormControl<string | null>(null),
    visibility: new FormControl<NoteVisibility>("PRIVATE", {
      nonNullable: true,
    }),
    classId: new FormControl<number | null>(null),
    lessonId: new FormControl<number | null>(null),
    type: new FormControl<NoteType>("OTHER", { nonNullable: true }),
    unit: new FormControl("", { nonNullable: true }),
    tags: new FormControl<string[]>([], { nonNullable: true }),
    favorite: new FormControl(false, { nonNullable: true }),
    content: new FormControl("", { nonNullable: true }),
  });

  ngOnInit() {
    this.api.languages().subscribe((languages) => this.languages.set(languages));
    this.api.classes().subscribe((classes) => this.classes.set(classes));
    this.api.tags().subscribe((tags) => this.tagCatalog.set(tags));
    this.loadLessonOptions();

    this.form.controls.languageId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((languageId) => {
        const levels = this.levelsFor(languageId);
        if (!levels.some((level) => level.code === this.form.controls.level.value)) {
          this.form.controls.level.setValue(levels[0]?.code ?? null);
        }
      });

    const id = this.id();
    if (id) {
      this.api.note(id).subscribe((note) => {
        this.form.patchValue({
          title: note.title,
          languageId: note.languageId,
          level: note.level,
          visibility: note.visibility,
          classId: note.classId,
          lessonId: note.lessonId,
          type: note.type,
          unit: note.unit ?? "",
          tags: note.tags,
          favorite: note.favorite,
          content: note.content ?? "",
        });
        this.version.set(note.version);
        this.lastSavedAt.set(note.lastSavedAt);
        this.ready = true;
        this.startSavePipeline();
      });
    } else {
      const draft = this.readDraft();
      if (draft) {
        this.form.patchValue(draft);
        this.draftRestored.set(true);
        this.saveState.set("draft");
      }
      this.ready = true;
      this.startSavePipeline();
    }
  }

  ngOnDestroy() {
    this.writeDraft();
  }

  @HostListener("window:beforeunload", ["$event"])
  warnBeforeUnload(event: BeforeUnloadEvent) {
    if (this.dirty) {
      this.writeDraft();
      event.preventDefault();
      event.returnValue = "";
    }
  }

  levelsFor(languageId: number | null): LanguageLevel[] {
    const language = this.languages().find((item) => item.id === languageId);
    return language?.levels?.length ? language.levels : DEFAULT_LEVELS;
  }

  levelLabel(code: string | null) {
    if (!code) return "";
    const all = this.languages().flatMap((language) => language.levels);
    return all.find((level) => level.code === code)?.label ?? code;
  }

  tagOptions() {
    const names = new Set(this.tagCatalog().map((tag) => tag.name));
    for (const tag of this.form.controls.tags.value) names.add(tag);
    return [...names].sort((a, b) => a.localeCompare(b));
  }

  saveStateLabel() {
    return this.locale.translate(`note.save.${this.saveState()}`);
  }

  saveStateIcon() {
    switch (this.saveState()) {
      case "saving":
        return "sync";
      case "saved":
        return "cloud_done";
      case "draft":
        return "description";
      case "conflict":
        return "sync_problem";
      case "error":
        return "cloud_off";
      default:
        return "cloud_queue";
    }
  }

  reload() {
    const id = this.id();
    if (!id) return;
    this.api.note(id).subscribe((note) => {
      this.form.patchValue({
        title: note.title,
        languageId: note.languageId,
        level: note.level,
        visibility: note.visibility,
        classId: note.classId,
        lessonId: note.lessonId,
        type: note.type,
        unit: note.unit ?? "",
        tags: note.tags,
        favorite: note.favorite,
        content: note.content ?? "",
      });
      this.version.set(note.version);
      this.lastSavedAt.set(note.lastSavedAt);
      this.conflict.set(false);
      this.saveState.set("saved");
    });
  }

  save() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const body: NoteRequest = {
      title: value.title.trim(),
      content: value.content,
      languageId: value.languageId,
      lessonId: value.lessonId,
      type: value.type,
      level: value.level,
      visibility: value.visibility,
      classId: value.classId,
      unit: value.unit.trim() || null,
      tags: value.tags,
      favorite: value.favorite,
    };
    (this.id()
      ? this.api.updateNote(this.id()!, body)
      : this.api.createNote(body)
    ).subscribe((note) => {
      this.clearDraft();
      this.dirty = false;
      this.saveState.set("saved");
      this.ui.toast(this.locale.translate("note.saved"));
      this.router.navigate(["/notes", note.id]);
    });
  }

  private startSavePipeline() {
    this.form.valueChanges
      .pipe(
        tap(() => {
          this.dirty = true;
          if (this.saveState() !== "conflict") this.saveState.set("unsaved");
          this.writeDraft();
        }),
        debounceTime(1200),
        switchMap(() => {
          if (this.saveState() === "conflict") return of(null);
          if (this.id() && this.form.controls.title.value.trim()) {
            return this.autosave();
          }
          this.saveState.set("draft");
          this.dirty = false;
          return of(null);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe();
  }

  private autosave() {
    this.saveState.set("saving");
    const value = this.form.getRawValue();
    return this.api
      .autosaveNote(this.id()!, {
        title: value.title,
        content: value.content,
        version: this.version(),
      })
      .pipe(
        tap((note) => {
          this.version.set(note.version);
          this.lastSavedAt.set(note.lastSavedAt);
          this.dirty = false;
          this.saveState.set("saved");
        }),
        catchError((error) => {
          if (error?.status === 409) {
            this.conflict.set(true);
            this.saveState.set("conflict");
          } else {
            this.saveState.set("error");
          }
          return of(null);
        }),
      );
  }

  private storageKey() {
    return `linguanotes.note-draft.${this.id() ?? "new"}`;
  }

  private writeDraft() {
    try {
      localStorage.setItem(
        this.storageKey(),
        JSON.stringify(this.form.getRawValue()),
      );
    } catch {
      // Quota dépassé : la sauvegarde serveur reste la source de vérité.
    }
  }

  private readDraft(): {
    title: string;
    languageId: number | null;
    level: string | null;
    visibility: NoteVisibility;
    lessonId: number | null;
    type: NoteType;
    unit: string;
    tags: string[];
    favorite: boolean;
    content: string;
  } | null {
    try {
      const raw = localStorage.getItem(this.storageKey());
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

  private clearDraft() {
    try {
      localStorage.removeItem(this.storageKey());
    } catch {
      // Rien à faire si le stockage local est indisponible.
    }
  }

  private loadLessonOptions() {
    this.api.courses().subscribe((courses) => {
      this.lessonOptions.set([]);
      for (const course of courses) {
        this.api.units(course.id).subscribe((units) => {
          for (const unit of units) {
            this.api.lessons(unit.id).subscribe((lessons) => {
              this.lessonOptions.update((items) => [
                ...items,
                ...lessons.map((lesson) => ({
                  id: lesson.id,
                  label: `${course.title} / ${unit.title} / ${lesson.title}`,
                })),
              ]);
            });
          }
        });
      }
    });
  }
}
