import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideZoneChangeDetection,
} from "@angular/core";
import {
  provideRouter,
  withComponentInputBinding,
  withInMemoryScrolling,
} from "@angular/router";
import {
  provideHttpClient,
  withInterceptors,
  withXsrfConfiguration,
} from "@angular/common/http";
import { provideAnimationsAsync } from "@angular/platform-browser/animations/async";
import { catchError, firstValueFrom, of } from "rxjs";
import { routes } from "./app.routes";
import { errorInterceptor } from "./core/interceptors/error.interceptor";
import { ApiService } from "./core/services/api.service";
import { AuthService } from "./core/services/auth.service";

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(
      routes,
      withComponentInputBinding(),
      withInMemoryScrolling({ scrollPositionRestoration: "enabled" }),
    ),
    provideHttpClient(
      withInterceptors([errorInterceptor]),
      withXsrfConfiguration({
        cookieName: "XSRF-TOKEN",
        headerName: "X-XSRF-TOKEN",
      }),
    ),
    provideAnimationsAsync(),
    provideAppInitializer(async () => {
      const api = inject(ApiService);
      const auth = inject(AuthService);
      // Récupère le jeton CSRF avant toute requête mutante, puis restaure
      // la session éventuelle pour que l'en-tête de l'application soit stable.
      await firstValueFrom(api.csrf().pipe(catchError(() => of(null))));
      await firstValueFrom(auth.restoreSession());
    }),
  ],
};
