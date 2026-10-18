import { Injectable, inject, signal } from "@angular/core";
import { Router } from "@angular/router";
import {
  Observable,
  catchError,
  map,
  of,
  shareReplay,
  switchMap,
  tap,
} from "rxjs";
import { Account, LoginRequest, RegisterRequest } from "../models";
import { ApiService } from "./api.service";
import { AppLocale, LocaleService } from "./locale.service";

@Injectable({ providedIn: "root" })
export class AuthService {
  private api = inject(ApiService);
  private router = inject(Router);
  private locale = inject(LocaleService);
  readonly account = signal<Account | null>(null);
  private restoreRequest: Observable<Account | null> | null = null;

  restoreSession() {
    const current = this.account();
    if (current) return of(current);
    if (!this.restoreRequest) {
      this.restoreRequest = this.api.me().pipe(
        map((account) => account as Account | null),
        tap((account) => this.account.set(account)),
        tap((account) => {
          if (account) this.syncPreference();
        }),
        catchError(() => {
          this.account.set(null);
          return of(null);
        }),
        shareReplay({ bufferSize: 1, refCount: false }),
      );
    }
    return this.restoreRequest.pipe(tap(() => (this.restoreRequest = null)));
  }

  login(request: LoginRequest) {
    return this.api.csrf().pipe(
      switchMap(() => this.api.login(request)),
      tap((account) => this.account.set(account)),
      tap(() => this.syncPreference()),
    );
  }

  register(request: RegisterRequest) {
    return this.api.csrf().pipe(
      switchMap(() => this.api.register(request)),
      switchMap(() =>
        this.login({ email: request.email, password: request.password }),
      ),
    );
  }

  logout() {
    return this.api.csrf().pipe(
      switchMap(() => this.api.logout()),
      tap(() => {
        this.account.set(null);
        this.router.navigateByUrl("/auth/login");
      }),
    );
  }

  private syncPreference() {
    this.api.preferences().subscribe((preference) => {
      if (preference.customized)
        this.locale.setLocale(preference.interfaceLocale as AppLocale, false);
    });
  }
}
