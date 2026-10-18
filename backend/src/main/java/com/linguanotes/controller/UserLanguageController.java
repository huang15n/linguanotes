package com.linguanotes.controller;

import com.linguanotes.dto.UserLanguageRequest;
import com.linguanotes.dto.UserLanguageResponse;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.UserLanguageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile/languages")
public class UserLanguageController {
    private final UserLanguageService languages;

    public UserLanguageController(UserLanguageService languages) {
        this.languages = languages;
    }

    @GetMapping
    public List<UserLanguageResponse> list(@AuthenticationPrincipal AccountPrincipal principal) {
        return languages.list(principal.id());
    }

    @PostMapping
    public ResponseEntity<UserLanguageResponse> save(@AuthenticationPrincipal AccountPrincipal principal,
                                                     @Valid @RequestBody UserLanguageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(languages.save(principal.id(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AccountPrincipal principal,
                                       @PathVariable Long id) {
        languages.delete(principal.id(), id);
        return ResponseEntity.noContent().build();
    }
}
