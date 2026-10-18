package com.linguanotes.controller;

import com.linguanotes.dto.Dto.PreferenceRequest;
import com.linguanotes.dto.Dto.UserPreferenceResponse;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.PreferenceService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/preferences")
public class PreferenceController {
    private final PreferenceService preferences;

    public PreferenceController(PreferenceService preferences) {
        this.preferences = preferences;
    }

    @GetMapping
    public UserPreferenceResponse get(@AuthenticationPrincipal AccountPrincipal principal) {
        return preferences.get(principal.id());
    }

    @PutMapping
    public UserPreferenceResponse update(@AuthenticationPrincipal AccountPrincipal principal,
                                         @Valid @RequestBody PreferenceRequest request) {
        return preferences.update(principal.id(), request);
    }
}