import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { MatSnackBar } from "@angular/material/snack-bar";
import { LocaleService } from "./locale.service";
import { catchError, throwError } from "rxjs";

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const sb = inject(MatSnackBar);
  const locale = inject(LocaleService);
  return next(req).pipe(
    catchError((e: HttpErrorResponse) => {
      if (e.status === 401 && req.url.includes("/api/auth/me")) {
        return throwError(() => e);
      }
      const msg =
        e.status === 0
          ? locale.translate("error.offline")
          : (e.error?.message ??
            locale
              .translate("error.generic")
              .replace("{status}", String(e.status)));
      sb.open(msg, "OK", { duration: 5000 });
      return throwError(() => e);
    }),
  );
};
