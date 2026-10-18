import { DatePipe } from "@angular/common";
import { Component, OnInit, computed, inject, signal } from "@angular/core";
import { RouterLink } from "@angular/router";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { ApiService } from "../../core/services/api.service";
import {
  LocaleService,
  TranslatePipe,
} from "../../core/services/locale.service";
import { AuthService } from "../../core/services/auth.service";
import { Dashboard, NOTE_TYPES, Note } from "../../core/models";

@Component({
  imports: [DatePipe, RouterLink, MatButtonModule, MatIconModule, TranslatePipe],
  styleUrl: "./dashboard.component.scss",
  templateUrl: "./dashboard.component.html",
})
export class DashboardComponent implements OnInit {
  private api = inject(ApiService);
  locale = inject(LocaleService);
  auth = inject(AuthService);
  data = signal<Dashboard | null>(null);
  loading = signal(true);
  types = NOTE_TYPES;
  tl = (type: string) => this.locale.translate(`type.${type}`);
  typeRows = computed(() => {
    const byType = this.data()?.notesByType ?? {};
    return this.types
      .map((type) => ({ ...type, count: byType[type.value] ?? 0 }))
      .filter((row) => row.count > 0)
      .sort((a, b) => b.count - a.count);
  });
  maxType = computed(() =>
    Math.max(1, ...this.typeRows().map((row) => row.count)),
  );

  ngOnInit() {
    this.api.dashboard().subscribe({
      next: (data) => this.data.set(data),
      complete: () => this.loading.set(false),
      error: () => this.loading.set(false),
    });
  }

  excerpt(note: Note) {
    return (note.content ?? "")
      .replace(/[#>*_`~|\[\]]|^-{3,}$|^[-+] /gm, "")
      .replace(/\s+/g, " ")
      .trim()
      .slice(0, 140);
  }

  count(type: string) {
    return this.data()?.notesByType[type] ?? 0;
  }
}
