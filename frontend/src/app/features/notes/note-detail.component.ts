import { Component, inject, input, OnInit, signal } from "@angular/core";
import { DatePipe } from "@angular/common";
import { Router, RouterLink } from "@angular/router";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatChipsModule } from "@angular/material/chips";
import { MarkdownPipe } from "../../shared/markdown/markdown.pipe";
import { ApiService } from "../../core/api.service";
import { LocaleService, TranslatePipe } from "../../core/locale.service";
import { UiService } from "../../shared/dialogs/dialogs";
import { Note } from "../../models";

@Component({
  imports: [
    MarkdownPipe,
    DatePipe,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    TranslatePipe,
  ],
  styleUrl: "./note-detail.component.scss",
  templateUrl: "./note-detail.component.html",
})
export class NoteDetailComponent implements OnInit {
  id = input.required<string>();
  private api = inject(ApiService);
  locale = inject(LocaleService);
  private ui = inject(UiService);
  private router = inject(Router);
  note = signal<Note | null>(null);
  tl = (type: string) => this.locale.translate(`type.${type}`);
  ngOnInit() {
    this.api.note(this.id()).subscribe((n) => this.note.set(n));
  }
  fav() {
    this.api.toggleFavorite(+this.id()).subscribe((n) => this.note.set(n));
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
