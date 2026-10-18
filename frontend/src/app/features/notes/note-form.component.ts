import { Component, inject, input, OnInit, signal } from "@angular/core";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { Router, RouterLink } from "@angular/router";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatSlideToggleModule } from "@angular/material/slide-toggle";
import { MarkdownEditorComponent } from "../../shared/markdown/markdown-editor.component";
import { ApiService } from "../../core/services/api.service";
import { LocaleService, TranslatePipe } from "../../core/services/locale.service";
import { UiService } from "../../shared/dialogs/dialogs";
import { Language, NOTE_TYPES, NoteRequest, NoteType } from "../../core/models";

@Component({
  imports: [
    MarkdownEditorComponent,
    TranslatePipe,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
  ],
  styleUrl: "./note-form.component.scss",
  templateUrl: "./note-form.component.html",
})
export class NoteFormComponent implements OnInit {
  id = input<string>();
  private api = inject(ApiService);
  private locale = inject(LocaleService);
  private ui = inject(UiService);
  private router = inject(Router);
  languages = signal<Language[]>([]);
  lessonOptions = signal<{ id: number; label: string }[]>([]);
  types = NOTE_TYPES;
  tl = (type: string) => this.locale.translate(`type.${type}`);
  form = new FormGroup({
    title: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(255)],
    }),
    languageId: new FormControl<number | null>(null),
    lessonId: new FormControl<number | null>(null),
    type: new FormControl<NoteType>("OTHER", { nonNullable: true }),
    unit: new FormControl("", { nonNullable: true }),
    tags: new FormControl("", { nonNullable: true }),
    favorite: new FormControl(false, { nonNullable: true }),
    content: new FormControl("", { nonNullable: true }),
  });
  ngOnInit() {
    this.api.languages().subscribe((l) => this.languages.set(l));
    this.loadLessonOptions();
    const id = this.id();
    if (id)
      this.api.note(id).subscribe((n) =>
        this.form.patchValue({
          title: n.title,
          languageId: n.languageId,
          lessonId: n.lessonId,
          type: n.type,
          unit: n.unit ?? "",
          tags: n.tags.join(", "),
          favorite: n.favorite,
          content: n.content ?? "",
        }),
      );
  }
  save() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const id = this.id();
    const body: NoteRequest = {
      title: v.title,
      content: v.content,
      languageId: v.languageId,
      lessonId: v.lessonId,
      type: v.type,
      unit: v.unit.trim() || null,
      tags: v.tags
        .split(",")
        .map((t) => t.trim())
        .filter(Boolean),
      favorite: v.favorite,
    };
    (id ? this.api.updateNote(id, body) : this.api.createNote(body)).subscribe(
      (n) => {
        this.ui.toast(this.locale.translate("note.saved"));
        this.router.navigate(["/notes", n.id]);
      },
    );
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
