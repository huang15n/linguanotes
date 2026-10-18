import { Component, inject, signal } from "@angular/core";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { catchError, map, of } from "rxjs";
import { LocaleService, TranslatePipe } from "../../core/locale.service";
import { AuthService } from "../../core/auth.service";

@Component({
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    TranslatePipe,
  ],
  styleUrl: "./auth-page.component.scss",
  templateUrl: "./auth-page.component.html",
})
export class AuthPageComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private locale = inject(LocaleService);
  mode =
    this.route.snapshot.paramMap.get("mode") === "register"
      ? "register"
      : "login";
  busy = signal(false);
  error = signal("");
  form = new FormGroup({
    name: new FormControl("", {
      nonNullable: true,
      validators: [Validators.maxLength(120)],
    }),
    email: new FormControl("", {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.email,
        Validators.maxLength(254),
      ],
    }),
    password: new FormControl("", {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.minLength(12),
        Validators.maxLength(64),
      ],
    }),
  });

  constructor() {
    if (this.mode === "register")
      this.form.controls.name.addValidators(Validators.required);
  }

  submit() {
    if (this.form.invalid || this.busy()) {
      this.form.markAllAsTouched();
      return;
    }
    this.busy.set(true);
    this.error.set("");
    const { name, email, password } = this.form.getRawValue();
    const request =
      this.mode === "register"
        ? this.auth.register({
            name: name.trim(),
            email: email.trim(),
            password,
          })
        : this.auth.login({ email: email.trim(), password });
    request
      .pipe(
        map(() => true),
        catchError((error) => {
          this.error.set(
            error.error?.message ?? this.locale.translate("auth.failed"),
          );
          return of(false);
        }),
      )
      .subscribe((success) => {
        this.busy.set(false);
        if (success) {
          const returnUrl = this.route.snapshot.queryParamMap.get("returnUrl");
          this.router.navigateByUrl(
            returnUrl?.startsWith("/") ? returnUrl : "/dashboard",
          );
        }
      });
  }
}
