import { Component, ElementRef, forwardRef, signal, viewChild } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MarkdownPipe } from './markdown.pipe';

type Mode = 'edit' | 'split' | 'preview';

@Component({
  selector: 'app-markdown-editor',
  imports: [MatButtonModule, MatButtonToggleModule, MatIconModule, MatTooltipModule, MarkdownPipe],
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => MarkdownEditorComponent), multi: true }],
  styles: [`
    :host { display: block; }
    .bar { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; justify-content: space-between; margin-bottom: 8px; }
    .tools { display: flex; flex-wrap: wrap; align-items: center; gap: 2px; }
    .tools .sep { width: 1px; height: 22px; background: var(--mat-sys-outline-variant); margin: 0 6px; }
    .tools .h { min-width: 36px; padding: 0 6px; font-weight: 600; }
    .panes { display: grid; grid-template-columns: 1fr; gap: 12px; }
    .panes.split { grid-template-columns: 1fr 1fr; }
    @media (max-width: 800px) { .panes.split { grid-template-columns: 1fr; } }
    textarea, .preview { box-sizing: border-box; width: 100%; min-height: 360px; border: 1px solid var(--mat-sys-outline-variant); border-radius: 8px; padding: 12px 14px; background: var(--mat-sys-surface-container-lowest); color: var(--mat-sys-on-surface); }
    textarea { resize: vertical; font: 14px/1.6 ui-monospace, SFMono-Regular, Menlo, monospace; }
    textarea:focus { outline: 2px solid var(--mat-sys-primary); outline-offset: -1px; }
    .preview { overflow: auto; }
    .empty { color: var(--mat-sys-on-surface-variant); }
  `],
  template: `
    <div class="bar">
      <div class="tools" (mousedown)="$event.preventDefault()">
        <button mat-icon-button type="button" matTooltip="Gras (Ctrl/⌘+B)" [disabled]="off()" (click)="wrap('**')"><mat-icon>format_bold</mat-icon></button>
        <button mat-icon-button type="button" matTooltip="Italique (Ctrl/⌘+I)" [disabled]="off()" (click)="wrap('*')"><mat-icon>format_italic</mat-icon></button>
        <button mat-icon-button type="button" matTooltip="Barré" [disabled]="off()" (click)="wrap('~~')"><mat-icon>strikethrough_s</mat-icon></button>
        <span class="sep"></span>
        <button mat-button type="button" class="h" matTooltip="Titre 1" [disabled]="off()" (click)="heading(1)">H1</button>
        <button mat-button type="button" class="h" matTooltip="Titre 2" [disabled]="off()" (click)="heading(2)">H2</button>
        <button mat-button type="button" class="h" matTooltip="Titre 3" [disabled]="off()" (click)="heading(3)">H3</button>
        <span class="sep"></span>
        <button mat-icon-button type="button" matTooltip="Liste à puces" [disabled]="off()" (click)="bullets()"><mat-icon>format_list_bulleted</mat-icon></button>
        <button mat-icon-button type="button" matTooltip="Liste numérotée" [disabled]="off()" (click)="numbered()"><mat-icon>format_list_numbered</mat-icon></button>
        <button mat-icon-button type="button" matTooltip="Citation" [disabled]="off()" (click)="quote()"><mat-icon>format_quote</mat-icon></button>
        <span class="sep"></span>
        <button mat-icon-button type="button" matTooltip="Code en ligne" [disabled]="off()" (click)="inlineCode()"><mat-icon>code</mat-icon></button>
        <button mat-icon-button type="button" matTooltip="Bloc de code" [disabled]="off()" (click)="codeBlock()"><mat-icon>integration_instructions</mat-icon></button>
        <button mat-icon-button type="button" matTooltip="Lien (Ctrl/⌘+K)" [disabled]="off()" (click)="link()"><mat-icon>link</mat-icon></button>
        <button mat-icon-button type="button" matTooltip="Tableau (vocabulaire)" [disabled]="off()" (click)="table()"><mat-icon>table_chart</mat-icon></button>
        <button mat-icon-button type="button" matTooltip="Ligne de séparation" [disabled]="off()" (click)="rule()"><mat-icon>horizontal_rule</mat-icon></button>
      </div>
      <mat-button-toggle-group [value]="mode()" (change)="mode.set($event.value)" hideSingleSelectionIndicator aria-label="Mode d'affichage">
        <mat-button-toggle value="edit">Écrire</mat-button-toggle>
        <mat-button-toggle value="split">Les deux</mat-button-toggle>
        <mat-button-toggle value="preview">Aperçu</mat-button-toggle>
      </mat-button-toggle-group>
    </div>
    <div class="panes" [class.split]="mode() === 'split'">
      @if (mode() !== 'preview') {
        <textarea #ta [value]="value()" [disabled]="disabled()" (input)="onInput($event)" (keydown)="onKey($event)" (blur)="onTouched()"
          placeholder="Écrivez en Markdown : **gras**, *italique*, # Titre, - liste, | tableau |"></textarea>
      }
      @if (mode() !== 'edit') {
        <div class="preview md">
          @if (value()) { <div [innerHTML]="value() | markdown"></div> } @else { <span class="empty">Rien à prévisualiser.</span> }
        </div>
      }
    </div>`
})
export class MarkdownEditorComponent implements ControlValueAccessor {
  ta = viewChild<ElementRef<HTMLTextAreaElement>>('ta');
  value = signal(''); mode = signal<Mode>('split'); disabled = signal(false);
  off = () => this.mode() === 'preview' || this.disabled();
  private onChange: (v: string) => void = () => {};
  onTouched: () => void = () => {};

  writeValue(v: string | null) { this.value.set(v ?? ''); }
  registerOnChange(fn: (v: string) => void) { this.onChange = fn; }
  registerOnTouched(fn: () => void) { this.onTouched = fn; }
  setDisabledState(d: boolean) { this.disabled.set(d); }

  onInput(e: Event) { this.set((e.target as HTMLTextAreaElement).value); }
  private set(v: string) { this.value.set(v); this.onChange(v); }

  onKey(e: KeyboardEvent) {
    if (!(e.ctrlKey || e.metaKey)) return;
    const k = e.key.toLowerCase();
    if (k === 'b') { e.preventDefault(); this.wrap('**'); }
    else if (k === 'i') { e.preventDefault(); this.wrap('*'); }
    else if (k === 'k') { e.preventDefault(); this.link(); }
  }

  private apply(fn: (t: string, s: number, e: number) => { text: string; start: number; end: number }) {
    const el = this.ta()?.nativeElement; if (!el) return;
    const r = fn(el.value, el.selectionStart, el.selectionEnd);
    el.value = r.text; this.set(r.text);
    setTimeout(() => { el.focus(); el.setSelectionRange(r.start, r.end); });
  }
  wrap(before: string, after = before, placeholder = 'texte') {
    this.apply((t, s, e) => {
      const sel = t.slice(s, e) || placeholder;
      return { text: t.slice(0, s) + before + sel + after + t.slice(e), start: s + before.length, end: s + before.length + sel.length };
    });
  }
  prefix(p: (i: number) => string) {
    this.apply((t, s, e) => {
      const ls = s === 0 ? 0 : t.lastIndexOf('\n', s - 1) + 1;
      let le = t.indexOf('\n', e); if (le < 0) le = t.length;
      const out = t.slice(ls, le).split('\n').map((l, i) => p(i) + l).join('\n');
      return { text: t.slice(0, ls) + out + t.slice(le), start: ls, end: ls + out.length };
    });
  }
  heading(n: number) { this.prefix(() => '#'.repeat(n) + ' '); }
  link() {
    this.apply((t, s, e) => {
      const sel = t.slice(s, e) || 'texte'; const us = s + sel.length + 3;
      return { text: t.slice(0, s) + `[${sel}](https://)` + t.slice(e), start: us, end: us + 8 };
    });
  }
  insert(snippet: string) {
    this.apply((t, s, e) => ({ text: t.slice(0, s) + snippet + t.slice(e), start: s + snippet.length, end: s + snippet.length }));
  }
  bullets() { this.prefix(() => '- '); }
  numbered() { this.prefix(i => (i + 1) + '. '); }
  quote() { this.prefix(() => '> '); }
  inlineCode() { this.wrap('`'); }
  codeBlock() { const fence = '\n```\n'; this.wrap(fence, fence, 'code'); }
  rule() { this.insert('\n\n---\n\n'); }
  table() { this.insert('\n| Mot | Traduction | Exemple |\n| --- | --- | --- |\n|  |  |  |\n'); }
}
