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
import { MatIconModule } from "@angular/material/icon";
import { MatTooltipModule } from "@angular/material/tooltip";
import { TranslatePipe } from "../../core/services/locale.service";
import { ApiService } from "../../core/services/api.service";

@Component({
  selector: "app-rich-text-editor",
  imports: [MatButtonModule, MatIconModule, MatTooltipModule, TranslatePipe],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => RichTextEditorComponent),
      multi: true,
    },
  ],
  styleUrl: "./rich-text-editor.component.scss",
  templateUrl: "./rich-text-editor.component.html",
})
export class RichTextEditorComponent implements ControlValueAccessor {
  private api = inject(ApiService);
  editor = viewChild<ElementRef<HTMLDivElement>>("editor");
  imageInput = viewChild<ElementRef<HTMLInputElement>>("imageInput");
  disabled = signal(false);
  imageUploading = signal(false);
  private onChange: (v: string) => void = () => {};
  onTouched: () => void = () => {};
  private lastHtml = "";

  writeValue(v: string | null) {
    const html = v ?? "";
    this.lastHtml = html;
    queueMicrotask(() => {
      const el = this.editor()?.nativeElement;
      if (el && el.innerHTML !== html) {
        el.innerHTML = html;
      }
    });
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

  onInput() {
    const el = this.editor()?.nativeElement;
    if (!el) return;
    this.lastHtml = el.innerHTML;
    this.onChange(this.lastHtml === "<br>" ? "" : this.lastHtml);
  }

  cmd(command: string, value?: string) {
    if (this.disabled()) return;
    this.editor()?.nativeElement.focus();
    document.execCommand(command, false, value);
    this.onInput();
  }

  heading(level: 1 | 2 | 3) {
    this.cmd("formatBlock", `H${level}`);
  }

  paragraph() {
    this.cmd("formatBlock", "P");
  }

  link() {
    const url = window.prompt("URL");
    if (url) this.cmd("createLink", url);
  }

  unlink() {
    this.cmd("unlink");
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
        this.cmd("insertImage", `/api/files/${stored.id}`);
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
