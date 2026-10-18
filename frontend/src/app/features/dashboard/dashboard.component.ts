import { Component, inject, OnInit, signal } from "@angular/core";
import { DatePipe } from "@angular/common";
import { RouterLink } from "@angular/router";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatChipsModule } from "@angular/material/chips";
import { MatListModule } from "@angular/material/list";
import { LocaleService, TranslatePipe } from "../../core/locale.service";
import { ApiService } from "../../core/api.service";
import { Dashboard, NOTE_TYPES } from "../../models";

@Component({
  imports: [
    DatePipe,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatListModule,
    TranslatePipe,
  ],
  styleUrl: "./dashboard.component.scss",
  templateUrl: "./dashboard.component.html",
})
export class DashboardComponent implements OnInit {
  private api = inject(ApiService);
  locale = inject(LocaleService);
  data = signal<Dashboard | null>(null);
  types = NOTE_TYPES;
  tl = (type: string) => this.locale.translate(`type.${type}`);
  ngOnInit() {
    this.api.dashboard().subscribe((d) => this.data.set(d));
  }
}
