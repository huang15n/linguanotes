import {
  Component,
  ElementRef,
  forwardRef,
  inject,
  signal,
  viewChild,
} from "@angular/core";
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatButtonToggleModule } from "@angular/material/button-toggle";
import { MatIconModule } from "@angular/material/icon";
import { MatTooltipModule } from "@angular/material/tooltip";
import { TranslatePipe } from "../../core/services/locale.service";
import { ApiService } from "../../core/services/api.service";
import { MarkdownPipe } from "./markdown.pipe";

type Mode = "edit" | "split" | "preview";

@Component({
  selector: "app-markdown-editor",
  imports: [
    MatButtonModule,
    MatButtonToggleModule,
    MatIconModule,
    MatTooltipModule,
    TranslatePipe,
    MarkdownPipe,
  ],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => MarkdownEditorComponent),
      multi: true,
    },
  ],
  styleUrl: "./markdown-editor.component.scss",
  templateUrl: "./markdown-editor.component.html",
})
export class MarkdownEditorComponent implements ControlValueAccessor {
  private api = inject(ApiService);
  ta = viewChild<ElementRef<HTMLTextAreaElement>>("ta");
  imageInput = viewChild<ElementRef<HTMLInputElement>>("imageInput");
  value = signal("");
  mode = signal<Mode>("split");
  disabled = signal(false);
  imageUploading = signal(false);
  off = () => this.mode() === "preview" || this.disabled();
  private onChange: (v: string) => void = () => {};
  onTouched: () => void = () => {};

  writeValue(v: string | null) {
    this.value.set(v ?? "");
  }
  registerOnChange(fn: (v: string) => void) {
    this.onChange = fn;
  }
  registerOnTouched(fn: () => void) {
    this.onTouched = fn;
  }
  setDisabledState(d: boolean) {
    this.disabled.set(d);
  }

  onInput(e: Event) {
    this.set((e.target as HTMLTextAreaElement).value);
  }
  private set(v: string) {
    this.value.set(v);
    this.onChange(v);
  }

  onKey(e: KeyboardEvent) {
    if (!(e.ctrlKey || e.metaKey)) return;
    const k = e.key.toLowerCase();
    if (k === "b") {
      e.preventDefault();
      this.wrap("**");
    } else if (k === "i") {
      e.preventDefault();
      this.wrap("*");
    } else if (k === "k") {
      e.preventDefault();
      this.link();
    }
  }

  private apply(
    fn: (
      t: string,
      s: number,
      e: number,
    ) => { text: string; start: number; end: number },
  ) {
    const el = this.ta()?.nativeElement;
    if (!el) return;
    const r = fn(el.value, el.selectionStart, el.selectionEnd);
    el.value = r.text;
    this.set(r.text);
    setTimeout(() => {
      el.focus();
      el.setSelectionRange(r.start, r.end);
    });
  }
  wrap(before: string, after = before, placeholder = "texte") {
    this.apply((t, s, e) => {
      const sel = t.slice(s, e) || placeholder;
      return {
        text: t.slice(0, s) + before + sel + after + t.slice(e),
        start: s + before.length,
        end: s + before.length + sel.length,
      };
    });
  }
  prefix(p: (i: number) => string) {
    this.apply((t, s, e) => {
      const ls = s === 0 ? 0 : t.lastIndexOf("\n", s - 1) + 1;
      let le = t.indexOf("\n", e);
      if (le < 0) le = t.length;
      const out = t
        .slice(ls, le)
        .split("\n")
        .map((l, i) => p(i) + l)
        .join("\n");
      return {
        text: t.slice(0, ls) + out + t.slice(le),
        start: ls,
        end: ls + out.length,
      };
    });
  }
  heading(n: number) {
    this.prefix(() => "#".repeat(n) + " ");
  }
  link() {
    this.apply((t, s, e) => {
      const sel = t.slice(s, e) || "texte";
      const us = s + sel.length + 3;
      return {
        text: t.slice(0, s) + `[${sel}](https://)` + t.slice(e),
        start: us,
        end: us + 8,
      };
    });
  }
  insert(snippet: string) {
    this.apply((t, s, e) => ({
      text: t.slice(0, s) + snippet + t.slice(e),
      start: s + snippet.length,
      end: s + snippet.length,
    }));
  }
  bullets() {
    this.prefix(() => "- ");
  }
  numbered() {
    this.prefix((i) => i + 1 + ". ");
  }
  quote() {
    this.prefix(() => "> ");
  }
  inlineCode() {
    this.wrap("`");
  }
  codeBlock() {
    const fence = "\n```\n";
    this.wrap(fence, fence, "code");
  }
  rule() {
    this.insert("\n\n---\n\n");
  }
  table() {
    this.insert(
      "\n| Mot | Traduction | Exemple |\n| --- | --- | --- |\n|  |  |  |\n",
    );
  }

  openImagePicker() {
    this.imageInput()?.nativeElement.click();
  }

  uploadImage(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.item(0);
    if (!file) return;
    this.imageUploading.set(true);
    this.api.uploadFile(file).subscribe({
      next: (stored) => {
        const alt = file.name.replace(/\.[^.]+$/, "");
        this.insert(`\n![${alt}](/api/files/${stored.id})\n`);
        input.value = "";
        this.imageUploading.set(false);
      },
      error: () => {
        input.value = "";
        this.imageUploading.set(false);
      },
    });
  }
}
