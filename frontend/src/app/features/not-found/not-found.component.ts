import { Component, inject } from "@angular/core";
import { RouterLink } from "@angular/router";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { TranslatePipe } from "../../core/services/locale.service";
import { AuthService } from "../../core/services/auth.service";

@Component({
  imports: [RouterLink, MatButtonModule, MatIconModule, TranslatePipe],
  styleUrl: "./not-found.component.scss",
  templateUrl: "./not-found.component.html",
})
export class NotFoundComponent {
  auth = inject(AuthService);
}
