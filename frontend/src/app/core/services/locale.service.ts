import { Injectable, Pipe, PipeTransform, inject, signal } from "@angular/core";
import { ApiService } from "./api.service";
import { messages as en } from "../i18n/en";
import { messages as es } from "../i18n/es";
import { messages as fr } from "../i18n/fr";
import { messages as zh } from "../i18n/zh-CN";

export type AppLocale = "fr" | "es" | "en" | "zh-CN";

export interface LocaleOption {
  code: AppLocale;
  label: string;
}

export const APP_LOCALES: LocaleOption[] = [
  { code: "fr", label: "Français" },
  { code: "es", label: "Español" },
  { code: "en", label: "English" },
  { code: "zh-CN", label: "简体中文" },
];

const messages: Record<AppLocale, Record<string, string>> = {
  fr,
  es,
  en,
  "zh-CN": zh,
};

@Injectable({ providedIn: "root" })
export class LocaleService {
  private api = inject(ApiService);
  readonly current = signal<AppLocale>(this.initialLocale());
  readonly locales = APP_LOCALES;

  setLocale(locale: AppLocale, persist = true) {
    this.current.set(locale);
    localStorage.setItem("linguanotes.locale", locale);
    if (persist)
      this.api
        .preferences()
        .subscribe((preference) =>
          this.api
            .updatePreferences(locale, preference.dailyGoalMinutes)
            .subscribe(),
        );
  }

  translate(key: string) {
    return messages[this.current()][key] ?? messages.en[key] ?? key;
  }

  dateLocale() {
    return {
      fr: "fr-FR",
      es: "es-ES",
      en: "en-US",
      "zh-CN": "zh-CN",
    }[this.current()];
  }

  private initialLocale(): AppLocale {
    const saved = localStorage.getItem("linguanotes.locale");
    if (saved && saved in messages) return saved as AppLocale;
    const language = navigator.language.toLowerCase();
    if (language.startsWith("es")) return "es";
    if (language.startsWith("en")) return "en";
    if (language.startsWith("zh")) return "zh-CN";
    return "fr";
  }
}

@Pipe({ name: "t", standalone: true, pure: false })
export class TranslatePipe implements PipeTransform {
  private locale = inject(LocaleService);

  transform(key: string) {
    return this.locale.translate(key);
  }
}
