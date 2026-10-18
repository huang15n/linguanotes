import { Component, inject, signal } from "@angular/core";
import { ActivatedRoute, RouterLink } from "@angular/router";
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
import { catchError, map, of, switchMap } from "rxjs";
import { ApiService } from "../../core/services/api.service";
import { LocaleService, TranslatePipe } from "../../core/services/locale.service";

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
  templateUrl: "./password-reset.component.html",
})
export class PasswordResetComponent {
  private api = inject(ApiService);
  private locale = inject(LocaleService);
  token = inject(ActivatedRoute).snapshot.queryParamMap.get("token");
  busy = signal(false);
  sent = signal(false);
  complete = signal(false);
  error = signal("");
  form = new FormGroup({
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

  submit() {
    if (this.busy()) return;
    const control = this.token
      ? this.form.controls.password
      : this.form.controls.email;
    if (control.invalid) {
      control.markAsTouched();
      return;
    }
    this.busy.set(true);
    this.error.set("");
    const request = this.api.csrf().pipe(
      switchMap(() =>
        this.token
          ? this.api.confirmPasswordReset(
              this.token!,
              this.form.controls.password.value,
            )
          : this.api.requestPasswordReset(
              this.form.controls.email.value.trim(),
            ),
      ),
      map(() => true),
      catchError((error) => {
        this.error.set(
          error.error?.message ?? this.locale.translate("auth.failed"),
        );
        return of(false);
      }),
    );
    request.subscribe((success) => {
      this.busy.set(false);
      if (success) {
        this.sent.set(!this.token);
        this.complete.set(!!this.token);
      }
    });
  }
}
