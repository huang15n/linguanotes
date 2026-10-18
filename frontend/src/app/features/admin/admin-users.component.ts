import { DatePipe } from "@angular/common";
import { Component, OnInit, inject, signal } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { ApiService } from "../../core/services/api.service";
import {
  LocaleService,
  TranslatePipe,
} from "../../core/services/locale.service";
import { Account, AccountRole, AccountStatus } from "../../core/models";
import { UiService } from "../../shared/dialogs/dialogs";

@Component({
  imports: [
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    TranslatePipe,
  ],
  styleUrl: "./admin-users.component.scss",
  templateUrl: "./admin-users.component.html",
})
export class AdminUsersComponent implements OnInit {
  private api = inject(ApiService);
  private locale = inject(LocaleService);
  private ui = inject(UiService);
  users = signal<Account[]>([]);
  query = signal("");
  roleFilter = signal<AccountRole | "">("");
  statusFilter = signal<AccountStatus | "">("");
  roles: AccountRole[] = ["USER", "TEACHER", "ADMIN"];
  statuses: AccountStatus[] = ["ACTIVE", "PENDING", "SUSPENDED", "CLOSED"];

  ngOnInit() {
    this.load();
  }

  load() {
    this.api
      .adminUsers(this.query(), this.roleFilter(), this.statusFilter())
      .subscribe((users) => this.users.set(users));
  }

  changeRole(user: Account, role: AccountRole) {
    if (user.role === role) return;
    this.api.updateUserRole(user.id, role).subscribe((updated) => {
      this.users.update((items) =>
        items.map((item) => (item.id === updated.id ? updated : item)),
      );
      this.ui.toast(this.locale.translate("admin.roleSaved"));
    });
  }

  changeStatus(user: Account, status: AccountStatus) {
    if (user.status === status) return;
    this.api.updateUserStatus(user.id, status).subscribe((updated) => {
      this.users.update((items) =>
        items.map((item) => (item.id === updated.id ? updated : item)),
      );
      this.ui.toast(this.locale.translate("admin.statusSaved"));
    });
  }
}
