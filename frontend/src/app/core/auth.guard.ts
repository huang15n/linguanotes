import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";
import { catchError, map, of } from "rxjs";
import { AuthService } from "./auth.service";

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
