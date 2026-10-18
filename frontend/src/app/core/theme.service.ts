import { Injectable, effect, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  dark = signal(localStorage.getItem('dark') ? localStorage.getItem('dark') === '1' : matchMedia('(prefers-color-scheme: dark)').matches);
  constructor() {
    effect(() => {
      document.documentElement.classList.toggle('dark', this.dark());
      document.documentElement.classList.toggle('light', !this.dark());
      localStorage.setItem('dark', this.dark() ? '1' : '0');
    });
  }
  toggle() { this.dark.update(d => !d); }
}
