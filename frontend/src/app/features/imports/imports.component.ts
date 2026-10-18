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
import { TranslatePipe } from "../../core/services/locale.service";
import { Course, ImportJob, Language } from "../../core/models";

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
  styleUrl: "./imports.component.scss",
  templateUrl: "./imports.component.html",
})
export class ImportsComponent implements OnInit {
  private api = inject(ApiService);
  jobs = signal<ImportJob[]>([]);
  courses = signal<Course[]>([]);
  languages = signal<Language[]>([]);
  selectedFile = signal<File | null>(null);
  activeJob = signal<ImportJob | null>(null);
  form = new FormGroup({
    courseId: new FormControl<number | null>(null),
    courseTitle: new FormControl("", { nonNullable: true }),
    languageId: new FormControl<number | null>(null),
    unitTitle: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(180)],
    }),
  });

  ngOnInit() {
    this.load();
    this.api.courses().subscribe((items) => this.courses.set(items));
    this.api.languages().subscribe((items) => this.languages.set(items));
  }

  load() {
    this.api.imports().subscribe((items) => this.jobs.set(items));
  }

  chooseFile(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedFile.set(input.files?.item(0) ?? null);
  }

  upload() {
    const file = this.selectedFile();
    if (!file) return;
    this.api.uploadImport(file).subscribe((job) => {
      this.activeJob.set(job);
      this.selectedFile.set(null);
      this.load();
    });
  }

  selectJob(job: ImportJob) {
    this.activeJob.set(job);
  }

  confirm() {
    const job = this.activeJob();
    if (!job || this.form.controls.unitTitle.invalid) {
      this.form.controls.unitTitle.markAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const body = {
      courseId: value.courseId,
      courseTitle: value.courseId === null ? value.courseTitle.trim() : null,
      languageId: value.courseId === null ? value.languageId : null,
      unitTitle: value.unitTitle.trim(),
    };
    this.api.confirmImport(job.id, body).subscribe((updated) => {
      this.activeJob.set(updated);
      this.load();
    });
  }
}
