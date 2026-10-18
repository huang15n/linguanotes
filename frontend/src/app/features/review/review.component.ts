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
import { ApiService } from "../../core/services/api.service";
import {
  LocaleService,
  TranslatePipe,
} from "../../core/services/locale.service";
import { DueCard } from "../../core/models";
import { UiService } from "../../shared/dialogs/dialogs";

@Component({
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    TranslatePipe,
  ],
  styleUrl: "./review.component.scss",
  templateUrl: "./review.component.html",
})
export class ReviewComponent implements OnInit {
  private api = inject(ApiService);
  private locale = inject(LocaleService);
  private ui = inject(UiService);
  due = signal<DueCard[]>([]);
  cards = signal<DueCard[]>([]);
  current = signal<DueCard | null>(null);
  revealed = signal(false);
  form = new FormGroup({
    frontText: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required],
    }),
    backText: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  ngOnInit() {
    this.load();
  }

  load() {
    this.api.dueCards().subscribe((items) => {
      this.due.set(items);
      this.current.set(items[0] ?? null);
      this.revealed.set(false);
    });
    this.api.flashcards().subscribe((items) =>
      this.cards.set(
        items.map((item) => ({
          flashcardId: item.id,
          lessonId: item.lessonId,
          frontText: item.frontText,
          backText: item.backText,
          repetitions: 0,
          correctCount: 0,
          incorrectCount: 0,
          nextDueAt: "",
        })),
      ),
    );
  }

  reveal() {
    this.revealed.set(true);
  }

  answer(rating: "AGAIN" | "HARD" | "GOOD" | "EASY") {
    const current = this.current();
    if (!current) return;
    this.api
      .answerCard(current.flashcardId, rating)
      .subscribe(() => this.load());
  }

  remove(card: DueCard) {
    this.ui
      .confirm(
        this.locale
          .translate("common.deleteConfirm")
          .replace("{name}", card.frontText),
      )
      .subscribe(() =>
        this.api.deleteFlashcard(card.flashcardId).subscribe(() => {
          this.ui.toast(this.locale.translate("common.deleted"));
          this.load();
        }),
      );
  }

  create() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    this.api
      .createFlashcard({
        lessonId: null,
        vocabularyEntryId: null,
        frontText: value.frontText.trim(),
        backText: value.backText.trim(),
      })
      .subscribe(() => {
        this.form.reset({ frontText: "", backText: "" });
        this.load();
      });
  }
}
