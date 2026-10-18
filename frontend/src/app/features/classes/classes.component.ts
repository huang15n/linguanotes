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
import { AuthService } from "../../core/services/auth.service";
import {
  ClassExercise,
  ClassMember,
  Exercise,
  SchoolClass,
} from "../../core/models";
import { UiService } from "../../shared/dialogs/dialogs";

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
    TranslatePipe,
  ],
  styleUrl: "./classes.component.scss",
  templateUrl: "./classes.component.html",
})
export class ClassesComponent implements OnInit {
  private api = inject(ApiService);
  private locale = inject(LocaleService);
  private ui = inject(UiService);
  auth = inject(AuthService);
  classes = signal<SchoolClass[]>([]);
  selected = signal<SchoolClass | null>(null);
  members = signal<ClassMember[]>([]);
  assignments = signal<ClassExercise[]>([]);
  exercises = signal<Exercise[]>([]);
  canCreate = computed(() => this.auth.account()?.role !== "USER");
  assignForm = new FormGroup({
    exerciseId: new FormControl<number | null>(null, Validators.required),
  });
  form = new FormGroup({
    name: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(180)],
    }),
    description: new FormControl("", { nonNullable: true }),
  });
  joinForm = new FormGroup({
    joinCode: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(12)],
    }),
  });

  ngOnInit() {
    this.load();
    this.api.exercises().subscribe((items) => this.exercises.set(items));
  }

  ownExercises() {
    const accountId = this.auth.account()?.id;
    return this.exercises().filter((exercise) => exercise.ownerId === accountId);
  }

  load() {
    this.api.classes().subscribe((items) => this.classes.set(items));
  }

  create() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    this.api
      .createClass({
        name: value.name.trim(),
        description: value.description.trim() || null,
      })
      .subscribe((created) => {
        this.classes.update((items) => [created, ...items]);
        this.form.reset({ name: "", description: "" });
        this.select(created);
      });
  }

  join() {
    if (this.joinForm.invalid) {
      this.joinForm.markAllAsTouched();
      return;
    }
    this.api.joinClass(this.joinForm.controls.joinCode.value.trim()).subscribe({
      next: (joined) => {
        this.ui.toast(this.locale.translate("classes.joined"));
        this.joinForm.reset({ joinCode: "" });
        this.load();
        this.select(joined);
      },
    });
  }

  select(schoolClass: SchoolClass) {
    this.selected.set(schoolClass);
    this.api.classMembers(schoolClass.id).subscribe((items) => this.members.set(items));
    this.api
      .classExercises(schoolClass.id)
      .subscribe((items) => this.assignments.set(items));
  }

  assignExercise() {
    const schoolClass = this.selected();
    const exerciseId = this.assignForm.controls.exerciseId.value;
    if (!schoolClass || exerciseId === null) return;
    this.api.assignClassExercise(schoolClass.id, exerciseId).subscribe((assignment) => {
      this.assignments.update((items) => [
        assignment,
        ...items.filter((item) => item.id !== assignment.id),
      ]);
      this.assignForm.reset({ exerciseId: null });
      this.ui.toast(this.locale.translate("classes.exerciseAssigned"));
    });
  }

  removeAssignment(assignment: ClassExercise) {
    const schoolClass = this.selected();
    if (!schoolClass) return;
    this.api
      .removeClassExercise(schoolClass.id, assignment.id)
      .subscribe(() =>
        this.assignments.update((items) =>
          items.filter((item) => item.id !== assignment.id),
        ),
      );
  }

  removeMember(member: ClassMember) {
    const schoolClass = this.selected();
    if (!schoolClass) return;
    this.ui
      .confirm(
        this.locale
          .translate("classes.removeConfirm")
          .replace("{name}", member.name),
      )
      .subscribe(() =>
        this.api
          .removeClassMember(schoolClass.id, member.userId)
          .subscribe(() => {
            this.members.update((items) =>
              items.filter((item) => item.userId !== member.userId),
            );
            this.classes.update((items) =>
              items.map((item) =>
                item.id === schoolClass.id
                  ? { ...item, memberCount: Math.max(0, item.memberCount - 1) }
                  : item,
              ),
            );
          }),
      );
  }

  removeClass() {
    const schoolClass = this.selected();
    if (!schoolClass) return;
    this.ui
      .confirm(
        this.locale
          .translate("classes.deleteConfirm")
          .replace("{name}", schoolClass.name),
      )
      .subscribe(() =>
        this.api.deleteClass(schoolClass.id).subscribe(() => {
          this.classes.update((items) =>
            items.filter((item) => item.id !== schoolClass.id),
          );
          this.selected.set(null);
          this.members.set([]);
          this.ui.toast(this.locale.translate("classes.deleted"));
        }),
      );
  }

  copyCode(code: string) {
    navigator.clipboard?.writeText(code);
    this.ui.toast(this.locale.translate("classes.codeCopied"));
  }
}
