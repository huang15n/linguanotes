import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const sb = inject(MatSnackBar);
  return next(req).pipe(catchError((e: HttpErrorResponse) => {
    const msg = e.status === 0 ? 'Serveur injoignable (le backend est-il démarré ?)' : (e.error?.message ?? `Erreur ${e.status}`);
    sb.open(msg, 'OK', { duration: 5000 });
    return throwError(() => e);
  }));
};
