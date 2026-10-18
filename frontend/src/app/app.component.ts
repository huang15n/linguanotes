import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { BreakpointObserver } from '@angular/cdk/layout';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { map } from 'rxjs';
import { ThemeService } from './core/theme.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatSidenavModule, MatToolbarModule, MatListModule, MatIconModule, MatButtonModule],
  styles: [`:host{display:block;height:100vh}mat-sidenav-container{height:100%}mat-sidenav{width:240px}
    .brand{display:flex;gap:8px;align-items:center;padding:20px 16px;font:var(--mat-sys-title-large)}
    mat-toolbar{position:sticky;top:0;z-index:2;background:var(--mat-sys-surface-container)}`],
  template: `<mat-sidenav-container>
    <mat-sidenav [mode]="mobile() ? 'over' : 'side'" [opened]="!mobile() || open()" (closedStart)="open.set(false)">
      <div class="brand"><mat-icon>translate</mat-icon> LinguaNotes</div>
      <mat-nav-list>
        <a mat-list-item routerLink="/dashboard" routerLinkActive="active" (click)="open.set(false)"><mat-icon matListItemIcon>dashboard</mat-icon><span matListItemTitle>Tableau de bord</span></a>
        <a mat-list-item routerLink="/notes" routerLinkActive="active" (click)="open.set(false)"><mat-icon matListItemIcon>description</mat-icon><span matListItemTitle>Notes</span></a>
        <a mat-list-item routerLink="/languages" routerLinkActive="active" (click)="open.set(false)"><mat-icon matListItemIcon>language</mat-icon><span matListItemTitle>Langues</span></a>
      </mat-nav-list>
    </mat-sidenav>
    <mat-sidenav-content>
      <mat-toolbar>
        @if (mobile()) { <button mat-icon-button (click)="open.set(!open())" aria-label="Menu"><mat-icon>menu</mat-icon></button> }
        <span class="spacer"></span>
        <button mat-icon-button (click)="theme.toggle()" aria-label="Thème"><mat-icon>{{ theme.dark() ? 'light_mode' : 'dark_mode' }}</mat-icon></button>
      </mat-toolbar>
      <router-outlet />
    </mat-sidenav-content>
  </mat-sidenav-container>`
})
export class AppComponent {
  theme = inject(ThemeService); open = signal(false);
  mobile = toSignal(inject(BreakpointObserver).observe('(max-width: 800px)').pipe(map(r => r.matches)), { initialValue: false });
}
