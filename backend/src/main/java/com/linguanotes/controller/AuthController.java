package com.linguanotes.controller;

import com.linguanotes.dto.*;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.AccountExportService;
import com.linguanotes.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AccountService accounts;
    private final AccountExportService exportService;
    private final AuthenticationManager authenticationManager;
    private final SessionAuthenticationStrategy sessionStrategy;
    private final SecurityContextRepository contextRepository;
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    public AuthController(AccountService accounts, AccountExportService exportService,
                          AuthenticationManager authenticationManager,
                          SessionAuthenticationStrategy sessionStrategy,
                          SecurityContextRepository contextRepository) {
        this.accounts = accounts;
        this.exportService = exportService;
        this.authenticationManager = authenticationManager;
        this.sessionStrategy = sessionStrategy;
        this.contextRepository = contextRepository;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken());
    }

    @PostMapping("/register")
    public ResponseEntity<AccountResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accounts.register(request));
    }

    @PostMapping("/login")
    public AccountResponse login(@Valid @RequestBody LoginRequest request,
                                 HttpServletRequest servletRequest, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email().trim().toLowerCase(), request.password()));
        sessionStrategy.onAuthentication(authentication, servletRequest, response);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, servletRequest, response);
        return accounts.getAccount(((AccountPrincipal) authentication.getPrincipal()).id());
    }

    @GetMapping("/me")
    public AccountResponse me(@AuthenticationPrincipal AccountPrincipal principal) {
        return accounts.getAccount(principal.id());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication, HttpServletRequest request,
                                       HttpServletResponse response) {
        logoutHandler.logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/profile")
    public AccountResponse updateProfile(@AuthenticationPrincipal AccountPrincipal principal,
                                         @Valid @RequestBody ProfileUpdateRequest request) {
        return accounts.updateProfile(principal.id(), request);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal AccountPrincipal principal,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        accounts.changePassword(principal.id(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        accounts.requestPasswordReset(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        accounts.confirmPasswordReset(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export")
    public Map<String, Object> export(@AuthenticationPrincipal AccountPrincipal principal) {
        return exportService.export(principal.id());
    }

    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal AccountPrincipal principal,
                                              @Valid @RequestBody DeleteAccountRequest request,
                                              Authentication authentication, HttpServletRequest servletRequest,
                                              HttpServletResponse servletResponse) {
        accounts.deleteAccount(principal.id(), request);
        logoutHandler.logout(servletRequest, servletResponse, authentication);
        return ResponseEntity.noContent().build();
    }
}