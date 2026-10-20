import { DatePipe } from "@angular/common";
import { Component, OnInit, computed, inject, signal } from "@angular/core";
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
import {
  LocaleService,
  TranslatePipe,
} from "../../core/services/locale.service";
import { UiService } from "../../shared/dialogs/dialogs";
import { MarkdownEditorComponent } from "../../shared/markdown/markdown-editor.component";
import { Course, Language } from "../../core/models";

@Component({
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MarkdownEditorComponent,
    TranslatePipe,
  ],
  styleUrl: "./courses.component.scss",
  templateUrl: "./courses.component.html",
})
export class CoursesComponent implements OnInit {
  private api = inject(ApiService);
  locale = inject(LocaleService);
  private ui = inject(UiService);
  courses = signal<Course[]>([]);
  languages = signal<Language[]>([]);
  search = signal("");
  languageFilter = signal<number | null>(null);
  levelFilter = signal("");
  selected = signal<Course | null>(null);
  creating = signal(false);
  saving = signal(false);
  form = new FormGroup({
    title: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(180)],
    }),
    languageId: new FormControl<number | null>(null, Validators.required),
    level: new FormControl("", { nonNullable: true }),
    content: new FormControl("", { nonNullable: true }),
    status: new FormControl<Course["status"]>("DRAFT", { nonNullable: true }),
  });
  filtered = computed(() => {
    const query = this.search().trim().toLowerCase();
    return this.courses().filter((course) => {
      if (query && !`${course.title} ${course.content ?? ""}`.toLowerCase().includes(query))
        return false;
      if (this.languageFilter() && course.languageId !== this.languageFilter())
        return false;
      if (this.levelFilter() && course.level !== this.levelFilter()) return false;
      return true;
    });
  });
  filterLevels = computed(() => {
    const language = this.languages().find(
      (item) => item.id === this.languageFilter(),
    );
    const source = language ? [language] : this.languages();
    const levels = new Set<string>();
    for (const item of source) {
      for (const level of item.levels) levels.add(level.label);
    }
    return [...levels];
  });

  ngOnInit() {
    this.api.languages().subscribe((items) => this.languages.set(items));
    this.load();
  }

  load() {
    this.api.courses().subscribe((items) => this.courses.set(items));
  }

  formLevels() {
    return (
      this.languages().find(
        (item) => item.id === this.form.controls.languageId.value,
      )?.levels ?? []
    );
  }

  openNew() {
    this.selected.set(null);
    this.creating.set(true);
    this.form.reset({
      title: "",
      languageId: null,
      level: "",
      content: "",
      status: "DRAFT",
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  edit(course: Course) {
    this.selected.set(course);
    this.creating.set(true);
    this.form.setValue({
      title: course.title,
      languageId: course.languageId,
      level: course.level ?? "",
      content: course.content ?? "",
      status: course.status,
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  cancel() {
    this.creating.set(false);
    this.selected.set(null);
  }

  save() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    if (value.languageId === null) return;
    const body = {
      title: value.title.trim(),
      languageId: value.languageId,
      level: value.level.trim() || null,
      description: null,
      content: value.content,
      status: value.status,
    };
    this.saving.set(true);
    const course = this.selected();
    const request = course
      ? this.api.updateCourse(course.id, body)
      : this.api.createCourse(body);
    request.subscribe({
      next: (saved) => {
        this.ui.toast(this.locale.translate("courses.saved"));
        this.creating.set(false);
        this.selected.set(null);
        this.load();
      },
      complete: () => this.saving.set(false),
      error: () => this.saving.set(false),
    });
  }

  remove(course: Course) {
    this.ui
      .confirm(
        this.locale.translate("common.deleteConfirm").replace("{name}", course.title),
      )
      .subscribe(() =>
        this.api.deleteCourse(course.id).subscribe(() => {
          this.courses.update((items) =>
            items.filter((item) => item.id !== course.id),
          );
          if (this.selected()?.id === course.id) this.cancel();
          this.ui.toast(this.locale.translate("common.deleted"));
        }),
      );
  }

  statusLabel(status: string) {
    return this.locale.translate(`course.status.${status}`);
  }
}
