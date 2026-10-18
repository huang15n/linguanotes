package com.linguanotes.controller;

import com.linguanotes.dto.AccountResponse;
import com.linguanotes.dto.RoleUpdateRequest;
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
    public List<AccountResponse> list() {
        return accounts.listAccounts();
    }

    @PutMapping("/{id}/role")
    public AccountResponse updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return accounts.updateRole(id, request.role());
    }
}