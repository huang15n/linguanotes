import { Component, OnInit, inject, signal } from "@angular/core";
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
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatSlideToggleModule } from "@angular/material/slide-toggle";
import { MatSnackBar } from "@angular/material/snack-bar";
import { ApiService } from "../../core/services/api.service";
import { AuthService } from "../../core/services/auth.service";
import {
  AppLocale,
  LocaleService,
  TranslatePipe,
} from "../../core/services/locale.service";
import { Language, UserLanguage } from "../../core/models";
import { UiService } from "../../shared/dialogs/dialogs";

type Tab = "profile" | "security" | "preferences" | "data";

@Component({
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    TranslatePipe,
  ],
  styleUrl: "./settings.component.scss",
  templateUrl: "./settings.component.html",
})
export class SettingsComponent implements OnInit {
  private api = inject(ApiService);
  auth = inject(AuthService);
  locale = inject(LocaleService);
  private router = inject(Router);
  private snack = inject(MatSnackBar);
  private ui = inject(UiService);
  tab = signal<Tab>("profile");
  languages = signal<Language[]>([]);
  userLanguages = signal<UserLanguage[]>([]);
  profile = new FormGroup({
    name: new FormControl(this.auth.account()?.name ?? "", {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(120)],
    }),
    firstName: new FormControl("", { nonNullable: true }),
    lastName: new FormControl("", { nonNullable: true }),
    displayName: new FormControl("", { nonNullable: true }),
    country: new FormControl("", { nonNullable: true }),
    timezone: new FormControl("", { nonNullable: true }),
    nativeLanguageId: new FormControl<number | null>(null),
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
  languageForm = new FormGroup({
    languageId: new FormControl<number | null>(null, Validators.required),
    level: new FormControl("", { nonNullable: true }),
    target: new FormControl(true, { nonNullable: true }),
  });
  deleteForm = new FormGroup({
    password: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  ngOnInit() {
    const account = this.auth.account();
    if (account) {
      this.profile.patchValue({
        name: account.name,
        firstName: account.firstName ?? "",
        lastName: account.lastName ?? "",
        displayName: account.displayName ?? "",
        country: account.country ?? "",
        timezone: account.timezone ?? Intl.DateTimeFormat().resolvedOptions().timeZone,
        nativeLanguageId: account.nativeLanguageId,
      });
    }
    this.api.languages().subscribe((items) => this.languages.set(items));
    this.api.userLanguages().subscribe((items) => this.userLanguages.set(items));
    this.api.preferences().subscribe((preference) => {
      this.preferences.setValue({
        interfaceLocale: preference.customized
          ? (preference.interfaceLocale as AppLocale)
          : this.locale.current(),
        dailyGoalMinutes: preference.dailyGoalMinutes,
      });
    });
  }

  languageLevels() {
    return (
      this.languages().find(
        (language) => language.id === this.languageForm.controls.languageId.value,
      )?.levels ?? []
    );
  }

  saveProfile() {
    if (this.profile.invalid) {
      this.profile.markAllAsTouched();
      return;
    }
    const value = this.profile.getRawValue();
    this.api
      .updateProfile({
        name: value.name.trim(),
        firstName: value.firstName.trim() || null,
        lastName: value.lastName.trim() || null,
        displayName: value.displayName.trim() || null,
        avatarUrl: this.auth.account()?.avatarUrl ?? null,
        country: value.country.trim() || null,
        timezone: value.timezone.trim() || null,
        nativeLanguageId: value.nativeLanguageId,
      })
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

  addLanguage() {
    if (this.languageForm.invalid) {
      this.languageForm.markAllAsTouched();
      return;
    }
    const value = this.languageForm.getRawValue();
    if (value.languageId === null) return;
    this.api
      .saveUserLanguage({
        languageId: value.languageId,
        level: value.level || null,
        target: value.target,
      })
      .subscribe(() => {
        this.languageForm.reset({ languageId: null, level: "", target: true });
        this.api
          .userLanguages()
          .subscribe((items) => this.userLanguages.set(items));
      });
  }

  removeLanguage(entry: UserLanguage) {
    this.api.deleteUserLanguage(entry.id).subscribe(() =>
      this.userLanguages.update((items) =>
        items.filter((item) => item.id !== entry.id),
      ),
    );
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
