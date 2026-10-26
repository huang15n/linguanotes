import { Component, OnInit, inject, signal } from "@angular/core";
import { ActivatedRoute, RouterLink } from "@angular/router";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatChipsModule } from "@angular/material/chips";
import { ApiService } from "../../core/services/api.service";
import { LocaleService, TranslatePipe } from "../../core/services/locale.service";
import { TestPreparation } from "../../core/models";
import { MarkdownPipe } from "../../shared/markdown/markdown.pipe";

@Component({
  imports: [RouterLink, MatButtonModule, MatIconModule, MatChipsModule, TranslatePipe, MarkdownPipe],
  styleUrl: "./test-prep-detail.component.scss",
  templateUrl: "./test-prep-detail.component.html",
})
export class TestPrepDetailComponent implements OnInit {
  private api = inject(ApiService);
  private route = inject(ActivatedRoute);
  private locale = inject(LocaleService);
  item = signal<TestPreparation | null>(null);

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get("id"));
    this.api.testPreparation(id).subscribe({
      next: (x) => this.item.set(x),
    });
  }

  moduleLabel(m: string) {
    return this.locale.translate(`testPrep.module.${m.toLowerCase()}`);
  }
  langLabel(l: string) {
    return this.locale.translate(`testPrep.lang.${l.toLowerCase()}`);
  }
}
