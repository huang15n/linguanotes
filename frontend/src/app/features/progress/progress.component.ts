import { Component, OnInit, inject, signal } from "@angular/core";
import { MatCardModule } from "@angular/material/card";
import { ApiService } from "../../core/services/api.service";
import { LocaleService, TranslatePipe } from "../../core/services/locale.service";
import { ProgressPoint, ProgressSummary } from "../../core/models";

@Component({
  imports: [MatCardModule, TranslatePipe],
  styleUrl: "./progress.component.scss",
  templateUrl: "./progress.component.html",
})
export class ProgressComponent implements OnInit {
  private api = inject(ApiService);
  private locale = inject(LocaleService);
  summary = signal<ProgressSummary | null>(null);
  history = signal<ProgressPoint[]>([]);

  ngOnInit() {
    this.api.progressSummary().subscribe((result) => this.summary.set(result));
    this.api.progressHistory().subscribe((result) => this.history.set(result));
  }

  date(value: string) {
    return new Intl.DateTimeFormat(this.locale.dateLocale(), {
      dateStyle: "medium",
    }).format(new Date(value));
  }
}
