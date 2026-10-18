import { DatePipe } from "@angular/common";
import { Component, ElementRef, OnInit, inject, signal, viewChild } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatIconModule } from "@angular/material/icon";
import { ApiService } from "../../core/services/api.service";
import { LocaleService, TranslatePipe } from "../../core/services/locale.service";
import { StoredFile } from "../../core/models";
import { UiService } from "../../shared/dialogs/dialogs";

@Component({
  imports: [
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    TranslatePipe,
  ],
  styleUrl: "./files.component.scss",
  templateUrl: "./files.component.html",
})
export class FilesComponent implements OnInit {
  private api = inject(ApiService);
  private locale = inject(LocaleService);
  private ui = inject(UiService);
  readonly fileInput = viewChild<ElementRef<HTMLInputElement>>("fileInput");
  files = signal<StoredFile[]>([]);
  selectedFile = signal<File | null>(null);
  busy = signal(false);

  ngOnInit() {
    this.load();
  }

  load() {
    this.api.files().subscribe((items) => this.files.set(items));
  }

  chooseFile(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedFile.set(input.files?.item(0) ?? null);
  }

  upload() {
    const file = this.selectedFile();
    if (!file || this.busy()) return;
    this.busy.set(true);
    this.api.uploadFile(file).subscribe({
      next: () => {
        this.ui.toast(this.locale.translate("files.uploaded"));
        this.selectedFile.set(null);
        const input = this.fileInput()?.nativeElement;
        if (input) input.value = "";
        this.load();
      },
      complete: () => this.busy.set(false),
      error: () => this.busy.set(false),
    });
  }

  download(file: StoredFile) {
    this.api.downloadFile(file.id).subscribe((blob) => {
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = file.originalName;
      link.click();
      URL.revokeObjectURL(url);
    });
  }

  remove(file: StoredFile) {
    this.ui
      .confirm(
        this.locale
          .translate("files.deleteConfirm")
          .replace("{name}", file.originalName),
      )
      .subscribe(() =>
        this.api.deleteFile(file.id).subscribe(() => {
          this.ui.toast(this.locale.translate("files.deleted"));
          this.load();
        }),
      );
  }

  size(bytes: number) {
    if (bytes < 1024) return `${bytes} o`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} Ko`;
    return `${(bytes / 1048576).toFixed(1)} Mo`;
  }
}
