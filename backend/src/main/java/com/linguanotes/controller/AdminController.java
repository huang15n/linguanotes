package com.linguanotes.controller;

import com.linguanotes.dto.Dto.AccountResponse;
import com.linguanotes.dto.Dto.RoleUpdateRequest;
import com.linguanotes.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {
    private final AccountService accounts;

    public AdminController(AccountService accounts) {
        this.accounts = accounts;
    }

    @PutMapping("/{id}/role")
    public AccountResponse updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return accounts.updateRole(id, request.role());
    }
}