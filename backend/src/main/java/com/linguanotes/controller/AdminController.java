package com.linguanotes.controller;

import com.linguanotes.dto.AccountResponse;
import com.linguanotes.dto.RoleUpdateRequest;
import com.linguanotes.dto.StatusUpdateRequest;
import com.linguanotes.model.AccountRole;
import com.linguanotes.model.AccountStatus;
import com.linguanotes.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {
    private final AccountService accounts;

    public AdminController(AccountService accounts) {
        this.accounts = accounts;
    }

    @GetMapping
    public List<AccountResponse> list(@RequestParam(required = false) String q,
                                      @RequestParam(required = false) AccountRole role,
                                      @RequestParam(required = false) AccountStatus status) {
        return accounts.searchAccounts(q, role, status);
    }

    @PutMapping("/{id}/status")
    public AccountResponse updateStatus(@PathVariable Long id,
                                        @Valid @RequestBody StatusUpdateRequest request) {
        return accounts.updateStatus(id, request.status());
    }

    @PutMapping("/{id}/role")
    public AccountResponse updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return accounts.updateRole(id, request.role());
    }
}
