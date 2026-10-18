import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { RouterLink } from "@angular/router";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { ApiService } from "../../core/services/api.service";
import {
  LocaleService,
  TranslatePipe,
} from "../../core/services/locale.service";
import { ProgressPoint, ProgressSummary } from "../../core/models";

@Component({
  imports: [RouterLink, MatButtonModule, MatIconModule, TranslatePipe],
  styleUrl: "./progress.component.scss",
  templateUrl: "./progress.component.html",
})
export class ProgressComponent implements OnInit {
  private api = inject(ApiService);
  locale = inject(LocaleService);
  summary = signal<ProgressSummary | null>(null);
  history = signal<ProgressPoint[]>([]);
  maxAttempts = computed(() =>
    Math.max(1, ...this.history().map((point) => point.attempts)),
  );
  hasData = computed(
    () =>
      this.history().length > 0 ||
      (this.summary()?.attemptCount ?? 0) > 0 ||
      (this.summary()?.noteCount ?? 0) > 0,
  );

  ngOnInit() {
    this.api.progressSummary().subscribe((result) => this.summary.set(result));
    this.api.progressHistory().subscribe((result) => this.history.set(result));
  }

  date(value: string) {
    return new Intl.DateTimeFormat(this.locale.dateLocale(), {
      dateStyle: "medium",
    }).format(new Date(value));
  }

  barHeight(point: ProgressPoint) {
    return Math.max(6, (point.attempts / this.maxAttempts()) * 100);
  }
}
