import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Router } from "@angular/router";
import { catchError, throwError } from "rxjs";
import { AuthService } from "../services/auth.service";
import { LocaleService } from "../services/locale.service";

export const errorInterceptor: HttpInterceptorFn = (request, next) => {
  const snackBar = inject(MatSnackBar);
  const locale = inject(LocaleService);
  const router = inject(Router);
  const auth = inject(AuthService);

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      // La restauration de session teste volontairement /auth/me : un 401
      // signifie simplement "non connecté" et ne doit rien afficher.
      if (error.status === 401 && request.url.includes("/api/auth/me")) {
        return throwError(() => error);
      }

      // Le formulaire de connexion affiche déjà le message d'erreur.
      if (error.status === 401 && request.url.includes("/api/auth/login")) {
        return throwError(() => error);
      }

      if (error.status === 401) {
        auth.clearSession();
        router.navigateByUrl("/auth/login");
        return throwError(() => error);
      }

      const message =
        error.status === 0
          ? locale.translate("error.offline")
          : (error.error?.message ??
            locale
              .translate("error.generic")
              .replace("{status}", String(error.status)));
      snackBar.open(message, "OK", { duration: 5000 });
      return throwError(() => error);
    }),
  );
};
