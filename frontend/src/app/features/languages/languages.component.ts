import { Component, inject, OnInit, signal } from "@angular/core";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { ApiService } from "../../core/services/api.service";
import { LocaleService, TranslatePipe } from "../../core/services/locale.service";
import { UiService } from "../../shared/dialogs/dialogs";
import { Language } from "../../core/models";

@Component({
  imports: [MatCardModule, MatButtonModule, MatIconModule, TranslatePipe],
  styleUrl: "./languages.component.scss",
  templateUrl: "./languages.component.html",
})
export class LanguagesComponent implements OnInit {
  private api = inject(ApiService);
  private ui = inject(UiService);
  private locale = inject(LocaleService);
  list = signal<Language[]>([]);
  ngOnInit() {
    this.load();
  }
  load() {
    this.api.languages().subscribe((l) => this.list.set(l));
  }
  edit(l?: Language) {
    this.ui
      .form({
        title: this.locale.translate(l ? "languages.edit" : "languages.new"),
        fields: [
          { key: "name", label: this.locale.translate("languages.name") },
          { key: "code", label: this.locale.translate("languages.code") },
        ],
        value: l ? { name: l.name, code: l.code } : undefined,
      })
      .subscribe((v) =>
        (l
          ? this.api.updateLanguage(l.id, v)
          : this.api.createLanguage(v)
        ).subscribe(() => {
          this.ui.toast(this.locale.translate("languages.saved"));
          this.load();
        }),
      );
  }
  remove(l: Language) {
    this.ui
      .confirm(
        this.locale
          .translate("languages.deleteConfirm")
          .replace("{name}", l.name),
      )
      .subscribe(() =>
        this.api.deleteLanguage(l.id).subscribe(() => {
          this.ui.toast(this.locale.translate("languages.deleted"));
          this.load();
        }),
      );
  }
}
