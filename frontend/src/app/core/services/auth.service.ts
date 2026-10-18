import { Injectable, computed, inject, signal } from "@angular/core";
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
  readonly isAuthenticated = computed(() => this.account() !== null);
  readonly isAdmin = computed(() => this.account()?.role === "ADMIN");
  private restoreRequest: Observable<Account | null> | null = null;

  /**
   * Charge la session une seule fois puis met le résultat en cache.
   * Les gardes de route peuvent donc s'enchaîner sans requête supplémentaire.
   */
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
    return this.restoreRequest;
  }

  /** Réinitialise l'état local après une déconnexion ou une session expirée. */
  clearSession() {
    this.account.set(null);
    this.restoreRequest = null;
  }

  login(request: LoginRequest) {
    return this.api.csrf().pipe(
      switchMap(() => this.api.login(request)),
      tap((account) => {
        this.account.set(account);
        this.restoreRequest = null;
      }),
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
    return this.api.logout().pipe(
      catchError(() => of(void 0)),
      tap(() => {
        this.clearSession();
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
