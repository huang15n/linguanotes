import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";
import { catchError, map, of } from "rxjs";
import { AuthService } from "../services/auth.service";

/**
 * Restaure la session côté serveur avant d'autoriser une route protégée.
 * En cas d'absence de session, l'utilisateur est redirigé vers la connexion
 * en conservant l'URL demandée.
 */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.restoreSession().pipe(
    map((account) =>
      account
        ? true
        : router.createUrlTree(["/auth/login"], {
            queryParams: { returnUrl: state.url },
          }),
    ),
    catchError(() => of(router.createUrlTree(["/auth/login"]))),
  );
};

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth
    .restoreSession()
    .pipe(
      map((account) =>
        account?.role === "ADMIN"
          ? true
          : router.createUrlTree(["/dashboard"]),
      ),
      catchError(() => of(router.createUrlTree(["/auth/login"]))),
    );
};
