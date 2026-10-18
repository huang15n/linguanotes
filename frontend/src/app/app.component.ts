import { Component, inject, signal } from "@angular/core";
import { toSignal } from "@angular/core/rxjs-interop";
import { BreakpointObserver } from "@angular/cdk/layout";
import { RouterLink, RouterLinkActive, RouterOutlet } from "@angular/router";
import { MatSidenavModule } from "@angular/material/sidenav";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatListModule } from "@angular/material/list";
import { MatIconModule } from "@angular/material/icon";
import { MatButtonModule } from "@angular/material/button";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatSelectModule } from "@angular/material/select";
import { map } from "rxjs";
import { LocaleService, TranslatePipe } from "./core/locale.service";
import { ThemeService } from "./core/theme.service";
import { AuthService } from "./core/auth.service";

@Component({
  selector: "app-root",
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatSelectModule,
    TranslatePipe,
  ],
  styleUrl: "./app.component.scss",
  templateUrl: "./app.component.html",
})
export class AppComponent {
  theme = inject(ThemeService);
  locale = inject(LocaleService);
  auth = inject(AuthService);
  open = signal(false);
  mobile = toSignal(
    inject(BreakpointObserver)
      .observe("(max-width: 800px)")
      .pipe(map((r) => r.matches)),
    { initialValue: false },
  );

  logout() {
    this.auth.logout().subscribe();
  }
}
