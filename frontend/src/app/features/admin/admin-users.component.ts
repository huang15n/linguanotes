import { Component, OnInit, inject, signal } from "@angular/core";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatSelectModule } from "@angular/material/select";
import { ApiService } from "../../core/services/api.service";
import { LocaleService, TranslatePipe } from "../../core/services/locale.service";
import { Account } from "../../core/models";
import { UiService } from "../../shared/dialogs/dialogs";

type Role = Account["role"];

@Component({
  imports: [MatCardModule, MatFormFieldModule, MatSelectModule, TranslatePipe],
  styleUrl: "./admin-users.component.scss",
  templateUrl: "./admin-users.component.html",
})
export class AdminUsersComponent implements OnInit {
  private api = inject(ApiService);
  private locale = inject(LocaleService);
  private ui = inject(UiService);
  users = signal<Account[]>([]);
  roles: Role[] = ["USER", "TEACHER", "ADMIN"];

  ngOnInit() {
    this.api.adminUsers().subscribe((users) => this.users.set(users));
  }

  changeRole(user: Account, role: Role) {
    if (user.role === role) return;
    this.api.updateUserRole(user.id, role).subscribe((updated) => {
      this.users.update((items) =>
        items.map((item) => (item.id === updated.id ? updated : item)),
      );
      this.ui.toast(this.locale.translate("admin.roleSaved"));
    });
  }
}
