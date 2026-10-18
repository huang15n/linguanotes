import { Component, OnInit, inject } from "@angular/core";
import { Router } from "@angular/router";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatSnackBar } from "@angular/material/snack-bar";
import { ApiService } from "../../core/services/api.service";
import { AuthService } from "../../core/services/auth.service";
import {
  AppLocale,
  LocaleService,
  TranslatePipe,
} from "../../core/services/locale.service";
import { UiService } from "../../shared/dialogs/dialogs";

@Component({
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    TranslatePipe,
  ],
  styleUrl: "./settings.component.scss",
  templateUrl: "./settings.component.html",
})
export class SettingsComponent implements OnInit {
  private api = inject(ApiService);
  auth = inject(AuthService);
  private locale = inject(LocaleService);
  private router = inject(Router);
  private snack = inject(MatSnackBar);
  private ui = inject(UiService);
  profile = new FormGroup({
    name: new FormControl(this.auth.account()?.name ?? "", {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(120)],
    }),
  });
  password = new FormGroup({
    currentPassword: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required],
    }),
    newPassword: new FormControl("", {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.minLength(12),
        Validators.maxLength(64),
      ],
    }),
  });
  preferences = new FormGroup({
    interfaceLocale: new FormControl<AppLocale>(this.locale.current(), {
      nonNullable: true,
    }),
    dailyGoalMinutes: new FormControl(15, {
      nonNullable: true,
      validators: [Validators.min(1), Validators.max(240)],
    }),
  });
  deleteForm = new FormGroup({
    password: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  ngOnInit() {
    this.api.preferences().subscribe((preference) => {
      this.preferences.setValue({
        interfaceLocale: preference.customized
          ? (preference.interfaceLocale as AppLocale)
          : this.locale.current(),
        dailyGoalMinutes: preference.dailyGoalMinutes,
      });
    });
  }

  saveProfile() {
    if (this.profile.invalid) {
      this.profile.markAllAsTouched();
      return;
    }
    this.api
      .updateProfile(this.profile.controls.name.value.trim())
      .subscribe((account) => {
        this.auth.account.set(account);
        this.snack.open(this.locale.translate("settings.profileSaved"), "OK", {
          duration: 2500,
        });
      });
  }

  changePassword() {
    if (this.password.invalid) {
      this.password.markAllAsTouched();
      return;
    }
    const value = this.password.getRawValue();
    this.api
      .changePassword(value.currentPassword, value.newPassword)
      .subscribe(() => {
        this.password.reset({ currentPassword: "", newPassword: "" });
        this.snack.open(this.locale.translate("settings.passwordSaved"), "OK", {
          duration: 2500,
        });
      });
  }

  savePreferences() {
    const value = this.preferences.getRawValue();
    this.api
      .updatePreferences(value.interfaceLocale, value.dailyGoalMinutes)
      .subscribe((preference) => {
        this.locale.setLocale(preference.interfaceLocale as AppLocale, false);
        this.snack.open(
          this.locale.translate("settings.preferencesSaved"),
          "OK",
          { duration: 2500 },
        );
      });
  }

  exportData() {
    this.api.exportAccount().subscribe((data) => {
      const blob = new Blob([JSON.stringify(data, null, 2)], {
        type: "application/json",
      });
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = "linguanotes-data.json";
      link.click();
      URL.revokeObjectURL(url);
    });
  }

  deleteAccount() {
    if (this.deleteForm.invalid) {
      this.deleteForm.markAllAsTouched();
      return;
    }
    this.ui
      .confirm(this.locale.translate("settings.deleteConfirm"))
      .subscribe(() =>
        this.api
          .deleteAccount(this.deleteForm.controls.password.value)
          .subscribe(() => {
            this.auth.account.set(null);
            this.router.navigateByUrl("/auth/login");
          }),
      );
  }
}
