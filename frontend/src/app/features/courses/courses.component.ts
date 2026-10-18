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
import { Observable, tap } from "rxjs";
import { ApiService } from "../../core/services/api.service";
import { LocaleService, TranslatePipe } from "../../core/services/locale.service";
import {
  Course,
  Language,
  Lesson,
  LessonSection,
  LessonSectionType,
  Unit,
} from "../../core/models";
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
  styleUrl: "./courses.component.scss",
  templateUrl: "./courses.component.html",
})
export class CoursesComponent implements OnInit {
  private api = inject(ApiService);
  private locale = inject(LocaleService);
  private ui = inject(UiService);
  courses = signal<Course[]>([]);
  languages = signal<Language[]>([]);
  selectedCourse = signal<Course | null>(null);
  units = signal<Unit[]>([]);
  selectedUnit = signal<Unit | null>(null);
  lessons = signal<Lesson[]>([]);
  selectedLesson = signal<Lesson | null>(null);
  sections = signal<LessonSection[]>([]);
  editingSectionId = signal<number | null>(null);
  loading = signal(true);
  sectionTypes: { value: LessonSectionType; label: string }[] = [
    { value: "COMMUNICATION", label: "section.communication" },
    { value: "DIALOGUE", label: "section.dialogue" },
    { value: "VOCABULARY", label: "section.vocabulary" },
    { value: "GRAMMAR", label: "section.grammar" },
    { value: "PHONETICS", label: "section.phonetics" },
    { value: "SPELLING", label: "section.spelling" },
    { value: "CULTURE", label: "section.culture" },
    { value: "EXERCISES", label: "section.exercises" },
    { value: "REFERENCE", label: "section.reference" },
    { value: "REVIEW", label: "section.review" },
    { value: "CUSTOM", label: "section.custom" },
  ];
  courseForm = new FormGroup({
    title: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(180)],
    }),
    languageId: new FormControl<number | null>(null, Validators.required),
    level: new FormControl("", {
      nonNullable: true,
      validators: [Validators.maxLength(40)],
    }),
  });
  unitForm = new FormGroup({
    title: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(180)],
    }),
  });
  lessonForm = new FormGroup({
    title: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(180)],
    }),
    content: new FormControl("", { nonNullable: true }),
  });
  sectionForm = new FormGroup({
    type: new FormControl<LessonSectionType>("GRAMMAR", { nonNullable: true }),
    title: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(180)],
    }),
    content: new FormControl("", { nonNullable: true }),
    position: new FormControl(0, { nonNullable: true }),
    visible: new FormControl(true, { nonNullable: true }),
  });

  ngOnInit() {
    this.api.languages().subscribe((items) => this.languages.set(items));
    this.loadCourses();
  }

  loadCourses() {
    this.loading.set(true);
    this.api.courses().subscribe({
      next: (items) => this.courses.set(items),
      complete: () => this.loading.set(false),
    });
  }

  createCourse() {
    if (this.courseForm.invalid) {
      this.courseForm.markAllAsTouched();
      return;
    }
    const value = this.courseForm.getRawValue();
    if (value.languageId === null) return;
    this.api
      .createCourse({
        title: value.title.trim(),
        languageId: value.languageId,
        level: value.level.trim() || null,
        description: null,
      })
      .subscribe((course) => {
        this.courses.update((items) => [course, ...items]);
        this.courseForm.reset({ title: "", languageId: null, level: "" });
        this.selectCourse(course);
      });
  }

  courseLevels(): string[] {
    const course = this.selectedCourse();
    const languageId =
      course?.languageId ?? this.courseForm.controls.languageId.value;
    return (
      this.languages().find((language) => language.id === languageId)?.levels ??
      []
    ).map((level) => level.label);
  }

  selectCourse(course: Course) {
    this.selectedCourse.set(course);
    this.selectedUnit.set(null);
    this.selectedLesson.set(null);
    this.lessons.set([]);
    this.sections.set([]);
    this.api.units(course.id).subscribe((items) => this.units.set(items));
  }

  createUnit() {
    const course = this.selectedCourse();
    if (!course || this.unitForm.invalid) {
      this.unitForm.markAllAsTouched();
      return;
    }
    this.api
      .createUnit(course.id, {
        title: this.unitForm.controls.title.value.trim(),
        position: this.units().length,
        description: null,
      })
      .subscribe((unit) => {
        this.units.update((items) => [...items, unit]);
        this.unitForm.reset({ title: "" });
        this.selectUnit(unit);
      });
  }

  selectUnit(unit: Unit) {
    this.selectedUnit.set(unit);
    this.selectedLesson.set(null);
    this.sections.set([]);
    this.api.lessons(unit.id).subscribe((items) => this.lessons.set(items));
  }

  selectLesson(lesson: Lesson) {
    this.selectedLesson.set(lesson);
    this.editingSectionId.set(null);
    this.api
      .lessonSections(lesson.id)
      .subscribe((sections) => this.sections.set(sections));
  }

  createLesson() {
    const unit = this.selectedUnit();
    if (!unit || this.lessonForm.invalid) {
      this.lessonForm.markAllAsTouched();
      return;
    }
    const value = this.lessonForm.getRawValue();
    this.api
      .createLesson(unit.id, {
        title: value.title.trim(),
        content: value.content,
        type: "LESSON",
        position: this.lessons().length,
      })
      .subscribe((lesson) => {
        this.lessons.update((items) => [...items, lesson]);
        this.lessonForm.reset({ title: "", content: "" });
        this.selectLesson(lesson);
      });
  }

  sectionTypeLabel(type: LessonSectionType) {
    return this.locale.translate(
      this.sectionTypes.find((item) => item.value === type)?.label ??
        "section.custom",
    );
  }

  editSection(section: LessonSection) {
    this.editingSectionId.set(section.id);
    this.sectionForm.setValue({
      type: section.type,
      title: section.title,
      content: section.content ?? "",
      position: section.position,
      visible: section.visible,
    });
  }

  cancelSectionEdit() {
    this.editingSectionId.set(null);
    this.sectionForm.reset({
      type: "GRAMMAR",
      title: "",
      content: "",
      position: this.sections().length,
      visible: true,
    });
  }

  saveSection() {
    const lesson = this.selectedLesson();
    if (!lesson || this.sectionForm.invalid) {
      this.sectionForm.markAllAsTouched();
      return;
    }
    const value = this.sectionForm.getRawValue();
    const body = {
      type: value.type,
      title: value.title.trim(),
      content: value.content,
      contentJson: null,
      position: value.position,
      visible: value.visible,
    };
    const editingId = this.editingSectionId();
    const request = editingId
      ? this.api.updateLessonSection(lesson.id, editingId, body)
      : this.api.createLessonSection(lesson.id, body);
    request.subscribe((section) => {
      this.sections.update((items) =>
        editingId
          ? items.map((item) => (item.id === section.id ? section : item))
          : [...items, section],
      );
      this.cancelSectionEdit();
      this.ui.toast(this.locale.translate("section.saved"));
    });
  }

  removeSection(section: LessonSection) {
    const lesson = this.selectedLesson();
    if (!lesson) return;
    this.confirmDelete(section.title, () =>
      this.api.deleteLessonSection(lesson.id, section.id).pipe(
        tap(() =>
          this.sections.update((items) =>
            items.filter((item) => item.id !== section.id),
          ),
        ),
      ),
    );
  }

  removeCourse() {
    const course = this.selectedCourse();
    if (!course) return;
    this.confirmDelete(course.title, () =>
      this.api.deleteCourse(course.id).pipe(
        tap(() => {
          this.courses.update((items) =>
            items.filter((item) => item.id !== course.id),
          );
          this.selectedCourse.set(null);
          this.units.set([]);
          this.selectedUnit.set(null);
          this.selectedLesson.set(null);
          this.lessons.set([]);
          this.sections.set([]);
        }),
      ),
    );
  }

  removeUnit(unit: Unit) {
    const course = this.selectedCourse();
    if (!course) return;
    this.confirmDelete(unit.title, () =>
      this.api.deleteUnit(course.id, unit.id).pipe(
        tap(() => {
          this.units.update((items) =>
            items.filter((item) => item.id !== unit.id),
          );
          if (this.selectedUnit()?.id === unit.id) {
            this.selectedUnit.set(null);
            this.selectedLesson.set(null);
            this.lessons.set([]);
            this.sections.set([]);
          }
        }),
      ),
    );
  }

  removeLesson(lesson: Lesson) {
    const unit = this.selectedUnit();
    if (!unit) return;
    this.confirmDelete(lesson.title, () =>
      this.api.deleteLesson(unit.id, lesson.id).pipe(
        tap(() => {
          this.lessons.update((items) =>
            items.filter((item) => item.id !== lesson.id),
          );
          if (this.selectedLesson()?.id === lesson.id) {
            this.selectedLesson.set(null);
            this.sections.set([]);
          }
        }),
      ),
    );
  }

  private confirmDelete(name: string, action: () => Observable<unknown>) {
    this.ui
      .confirm(
        this.locale.translate("common.deleteConfirm").replace("{name}", name),
      )
      .subscribe(() =>
        action().subscribe(() =>
          this.ui.toast(this.locale.translate("common.deleted")),
        ),
      );
  }

  statusLabel(status: string) {
    return this.locale.translate(`course.status.${status}`);
  }
}
