import { Component, inject, signal } from "@angular/core";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { ApiService } from "../../core/services/api.service";
import { TranslatePipe } from "../../core/services/locale.service";
import { AiGeneration } from "../../core/models";

@Component({
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    TranslatePipe,
  ],
  styleUrl: "./ai.component.scss",
  templateUrl: "./ai.component.html",
})
export class AiComponent {
  private api = inject(ApiService);
  result = signal<AiGeneration | null>(null);
  busy = signal(false);
  kind = new FormControl("explain", { nonNullable: true });
  form = new FormGroup({
    content: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(10000)],
    }),
    targetLanguage: new FormControl("", { nonNullable: true }),
    level: new FormControl("", { nonNullable: true }),
  });

  generate() {
    if (this.form.invalid || this.busy()) {
      this.form.markAllAsTouched();
      return;
    }
    this.busy.set(true);
    this.result.set(null);
    this.api
      .ai(
        this.kind.value as "explain" | "generate-exercise" | "enrich-note",
        this.form.getRawValue(),
      )
      .subscribe({
        next: (result) => this.result.set(result),
        complete: () => this.busy.set(false),
        error: () => this.busy.set(false),
      });
  }
}
