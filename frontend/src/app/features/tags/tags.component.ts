import { Component, OnInit, inject, signal } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { ApiService } from "../../core/services/api.service";
import {
  LocaleService,
  TranslatePipe,
} from "../../core/services/locale.service";
import { Tag } from "../../core/models";
import { UiService } from "../../shared/dialogs/dialogs";

@Component({
  imports: [MatButtonModule, MatIconModule, TranslatePipe],
  styleUrl: "./tags.component.scss",
  templateUrl: "./tags.component.html",
})
export class TagsComponent implements OnInit {
  private api = inject(ApiService);
  private locale = inject(LocaleService);
  private ui = inject(UiService);
  tags = signal<Tag[]>([]);

  ngOnInit() {
    this.load();
  }

  load() {
    this.api.tags().subscribe((tags) => this.tags.set(tags));
  }

  edit(tag?: Tag) {
    this.ui
      .form({
        title: this.locale.translate(tag ? "tags.edit" : "tags.new"),
        fields: [
          { key: "name", label: this.locale.translate("tags.name") },
        ],
        value: tag ? { name: tag.name } : undefined,
      })
      .subscribe((value) => {
        const request = tag
          ? this.api.updateTag(tag.id, value["name"])
          : this.api.createTag(value["name"]);
        request.subscribe(() => {
          this.ui.toast(this.locale.translate("tags.saved"));
          this.load();
        });
      });
  }

  remove(tag: Tag) {
    this.ui
      .confirm(
        this.locale.translate("tags.deleteConfirm").replace("{name}", tag.name),
      )
      .subscribe(() =>
        this.api.deleteTag(tag.id).subscribe(() => {
          this.ui.toast(this.locale.translate("tags.deleted"));
          this.load();
        }),
      );
  }
}
