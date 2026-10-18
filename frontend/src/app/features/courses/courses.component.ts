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
import { Course, Language, Lesson, Unit } from "../../core/models";
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
  loading = signal(true);
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

  selectCourse(course: Course) {
    this.selectedCourse.set(course);
    this.selectedUnit.set(null);
    this.lessons.set([]);
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
    this.api.lessons(unit.id).subscribe((items) => this.lessons.set(items));
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
      });
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
          this.lessons.set([]);
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
            this.lessons.set([]);
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
        tap(() =>
          this.lessons.update((items) =>
            items.filter((item) => item.id !== lesson.id),
          ),
        ),
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
