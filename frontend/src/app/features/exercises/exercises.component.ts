import { DatePipe } from "@angular/common";
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
import { ApiService } from "../../core/api.service";
import { TranslatePipe } from "../../core/locale.service";
import { Exercise, ExerciseResult } from "../../models";

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
  styleUrl: "./exercises.component.scss",
  templateUrl: "./exercises.component.html",
})
export class ExercisesComponent implements OnInit {
  private api = inject(ApiService);
  exercises = signal<Exercise[]>([]);
  selected = signal<Exercise | null>(null);
  result = signal<ExerciseResult | null>(null);
  history = signal<
    {
      id: number;
      exerciseId: number;
      exerciseTitle: string;
      score: number;
      attemptedAt: string;
    }[]
  >([]);
  answer = new FormControl("", {
    nonNullable: true,
    validators: [Validators.required],
  });
  createForm = new FormGroup({
    title: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(180)],
    }),
    instructions: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required],
    }),
    type: new FormControl("MULTIPLE_CHOICE", { nonNullable: true }),
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
  });

  ngOnInit() {
    this.api.exercises().subscribe((items) => this.exercises.set(items));
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
        type: value.type,
        questions: [
          {
            prompt: value.prompt.trim(),
            options: value.options
              .split("\n")
              .map((option) => option.trim())
              .filter(Boolean),
            answer: value.expectedAnswer.trim(),
            explanation: value.explanation.trim(),
            points: 1,
          },
        ],
      })
      .subscribe((exercise) => {
        this.exercises.update((items) => [exercise, ...items]);
        this.select(exercise);
        this.createForm.reset({
          title: "",
          instructions: "",
          type: "MULTIPLE_CHOICE",
          prompt: "",
          options: "",
          expectedAnswer: "",
          explanation: "",
        });
      });
  }

  select(exercise: Exercise) {
    this.selected.set(exercise);
    this.result.set(null);
    this.answer.reset("");
    this.api
      .exerciseAttempts(exercise.id)
      .subscribe((items) => this.history.set(items));
  }

  submit() {
    const exercise = this.selected();
    if (!exercise || this.answer.invalid) {
      this.answer.markAsTouched();
      return;
    }
    this.api
      .submitExercise(exercise.id, [this.answer.value])
      .subscribe((result) => this.result.set(result));
  }
}
