import { DatePipe } from "@angular/common";
import { Component, OnInit, computed, inject, input, signal } from "@angular/core";
import { FormControl, ReactiveFormsModule, Validators } from "@angular/forms";
import { Router, RouterLink } from "@angular/router";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatChipsModule } from "@angular/material/chips";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MarkdownPipe } from "../../shared/markdown/markdown.pipe";
import { ApiService } from "../../core/services/api.service";
import {
  LocaleService,
  TranslatePipe,
} from "../../core/services/locale.service";
import { AuthService } from "../../core/services/auth.service";
import { UiService } from "../../shared/dialogs/dialogs";
import { Note, NoteComment } from "../../core/models";

@Component({
  imports: [
    MarkdownPipe,
    DatePipe,
    RouterLink,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatChipsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatTooltipModule,
    TranslatePipe,
  ],
  styleUrl: "./note-detail.component.scss",
  templateUrl: "./note-detail.component.html",
})
export class NoteDetailComponent implements OnInit {
  id = input.required<string>();
  private api = inject(ApiService);
  private ui = inject(UiService);
  private router = inject(Router);
  locale = inject(LocaleService);
  auth = inject(AuthService);
  note = signal<Note | null>(null);
  comments = signal<NoteComment[]>([]);
  editingComment = signal<NoteComment | null>(null);
  editText = signal("");
  commentControl = new FormControl("", {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(4000)],
  });
  isOwner = computed(
    () => this.note()?.authorId === this.auth.account()?.id,
  );
  tl = (type: string) => this.locale.translate(`type.${type}`);

  ngOnInit() {
    this.load();
    this.loadComments();
  }

  load() {
    this.api.note(this.id()).subscribe((note) => this.note.set(note));
  }

  loadComments() {
    this.api
      .noteComments(+this.id())
      .subscribe((comments) => this.comments.set(comments));
  }

  visibilityLabel(visibility: string) {
    return this.locale.translate(`note.visibility${visibility.charAt(0)}${visibility.slice(1).toLowerCase()}`);
  }

  toggleLike() {
    const note = this.note();
    if (!note) return;
    const request = note.likedByMe
      ? this.api.unlikeNote(note.id)
      : this.api.likeNote(note.id);
    request.subscribe((result) =>
      this.note.update((current) =>
        current
          ? {
              ...current,
              likedByMe: result.likedByMe,
              likeCount: result.likeCount,
            }
          : current,
      ),
    );
  }

  addComment() {
    if (this.commentControl.invalid) {
      this.commentControl.markAsTouched();
      return;
    }
    this.api
      .addNoteComment(+this.id(), this.commentControl.value.trim())
      .subscribe((comment) => {
        this.comments.update((items) => [...items, comment]);
        this.commentControl.reset("");
      });
  }

  startEdit(comment: NoteComment) {
    this.editingComment.set(comment);
    this.editText.set(comment.content);
  }

  cancelEdit() {
    this.editingComment.set(null);
    this.editText.set("");
  }

  saveEdit(comment: NoteComment) {
    const content = this.editText().trim();
    if (!content) return;
    this.api
      .updateNoteComment(+this.id(), comment.id, content)
      .subscribe((updated) => {
        this.comments.update((items) =>
          items.map((item) => (item.id === updated.id ? updated : item)),
        );
        this.cancelEdit();
      });
  }

  removeComment(comment: NoteComment) {
    this.ui
      .confirm(this.locale.translate("note.commentDeleteConfirm"))
      .subscribe(() =>
        this.api.deleteNoteComment(+this.id(), comment.id).subscribe(() => {
          this.comments.update((items) =>
            items.filter((item) => item.id !== comment.id),
          );
        }),
      );
  }

  fav() {
    this.api
      .toggleFavorite(+this.id())
      .subscribe((note) => this.note.set(note));
  }

  remove() {
    this.ui.confirm(this.locale.translate("note.deleteConfirm")).subscribe(() =>
      this.api.deleteNote(+this.id()).subscribe(() => {
        this.ui.toast(this.locale.translate("note.deleted"));
        this.router.navigate(["/notes"]);
      }),
    );
  }
}
