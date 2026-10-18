import { DatePipe } from "@angular/common";
import { Component, OnInit, inject, signal } from "@angular/core";
import {
  FormArray,
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
import { MatSlideToggleModule } from "@angular/material/slide-toggle";
import { ApiService } from "../../core/services/api.service";
import {
  LocaleService,
  TranslatePipe,
} from "../../core/services/locale.service";
import {
  Exercise,
  ExerciseAttemptSummary,
  ExerciseResult,
  ExerciseStatus,
  Language,
} from "../../core/models";
import { UiService } from "../../shared/dialogs/dialogs";

type QuestionForm = FormGroup<{
  prompt: FormControl<string>;
  options: FormControl<string>;
  expectedAnswer: FormControl<string>;
  explanation: FormControl<string>;
  points: FormControl<number>;
}>;

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
    MatSlideToggleModule,
    TranslatePipe,
  ],
  styleUrl: "./exercises.component.scss",
  templateUrl: "./exercises.component.html",
})
export class ExercisesComponent implements OnInit {
  private api = inject(ApiService);
  private locale = inject(LocaleService);
  private ui = inject(UiService);
  exercises = signal<Exercise[]>([]);
  languages = signal<Language[]>([]);
  selected = signal<Exercise | null>(null);
  result = signal<ExerciseResult | null>(null);
  history = signal<ExerciseAttemptSummary[]>([]);
  preview = signal(false);
  answers = new FormArray<FormControl<string>>([]);
  createForm = new FormGroup({
    title: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(180)],
    }),
    instructions: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required],
    }),
    languageId: new FormControl<number | null>(null),
    level: new FormControl("", { nonNullable: true }),
    grammarTopic: new FormControl("", { nonNullable: true }),
    introduction: new FormControl("", { nonNullable: true }),
    status: new FormControl<ExerciseStatus>("PUBLISHED", { nonNullable: true }),
    maxAttempts: new FormControl<number | null>(null),
    passThreshold: new FormControl(60, { nonNullable: true }),
    showAnswers: new FormControl(true, { nonNullable: true }),
    type: new FormControl("MULTIPLE_CHOICE", { nonNullable: true }),
    questions: new FormArray<QuestionForm>([this.questionGroup()]),
  });

  get questions() {
    return this.createForm.controls.questions;
  }

  ngOnInit() {
    this.api.exercises().subscribe((items) => this.exercises.set(items));
    this.api.languages().subscribe((items) => this.languages.set(items));
  }

  levels() {
    const language = this.languages().find(
      (item) => item.id === this.createForm.controls.languageId.value,
    );
    return language?.levels ?? [];
  }

  private questionGroup(): QuestionForm {
    return new FormGroup({
      prompt: new FormControl("", {
        nonNullable: true,
        validators: [Validators.required],
      }),
      options: new FormControl("", { nonNullable: true }),
      expectedAnswer: new FormControl("", {
        nonNullable: true,
        validators: [Validators.required],
      }),
      explanation: new FormControl("", { nonNullable: true }),
      points: new FormControl(1, {
        nonNullable: true,
        validators: [Validators.min(1), Validators.max(100)],
      }),
    });
  }

  addQuestion() {
    this.questions.push(this.questionGroup());
  }

  removeQuestion(index: number) {
    if (this.questions.length > 1) this.questions.removeAt(index);
  }

  create() {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }
    const value = this.createForm.getRawValue();
    this.api
      .createExercise({
        title: value.title.trim(),
        instructions: value.instructions.trim(),
        lessonId: null,
        languageId: value.languageId,
        level: value.level || null,
        grammarTopic: value.grammarTopic.trim() || null,
        introduction: value.introduction.trim() || null,
        status: value.status,
        maxAttempts: value.maxAttempts,
        passThreshold: value.passThreshold,
        showAnswers: value.showAnswers,
        type: value.type,
        questions: value.questions.map((question) => ({
          prompt: question.prompt.trim(),
          options: question.options
            .split("\n")
            .map((option) => option.trim())
            .filter(Boolean),
          answer: question.expectedAnswer.trim(),
          explanation: question.explanation.trim(),
          points: question.points,
        })),
      })
      .subscribe((exercise) => {
        this.exercises.update((items) => [exercise, ...items]);
        this.select(exercise);
        this.createForm.reset({
          title: "",
          instructions: "",
          languageId: null,
          level: "",
          grammarTopic: "",
          introduction: "",
          status: "PUBLISHED",
          maxAttempts: null,
          passThreshold: 60,
          showAnswers: true,
          type: "MULTIPLE_CHOICE",
        });
        this.questions.clear();
        this.questions.push(this.questionGroup());
      });
  }

  select(exercise: Exercise) {
    this.selected.set(exercise);
    this.result.set(null);
    this.answers.clear();
    for (const question of exercise.questions) {
      this.answers.push(
        new FormControl("", {
          nonNullable: true,
          validators: [Validators.required],
        }),
      );
    }
    this.api
      .exerciseAttempts(exercise.id)
      .subscribe((items) => this.history.set(items));
  }

  submit() {
    const exercise = this.selected();
    if (!exercise) return;
    if (this.answers.invalid) {
      this.answers.markAllAsTouched();
      return;
    }
    this.api
      .submitExercise(exercise.id, this.answers.getRawValue(), this.preview())
      .subscribe((result) => {
        this.result.set(result);
        this.api
          .exerciseAttempts(exercise.id)
          .subscribe((items) => this.history.set(items));
      });
  }

  removeExercise() {
    const exercise = this.selected();
    if (!exercise) return;
    this.ui
      .confirm(
        this.locale
          .translate("exercises.deleteConfirm")
          .replace("{title}", exercise.title),
      )
      .subscribe(() =>
        this.api.deleteExercise(exercise.id).subscribe(() => {
          this.exercises.update((items) =>
            items.filter((item) => item.id !== exercise.id),
          );
          this.selected.set(null);
          this.result.set(null);
          this.history.set([]);
          this.ui.toast(this.locale.translate("exercises.deleted"));
        }),
      );
  }
}
