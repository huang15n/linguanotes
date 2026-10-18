package com.linguanotes.controller;

import com.linguanotes.dto.AiGenerationResponse;
import com.linguanotes.dto.AiRequest;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.AiService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiService ai;

    public AiController(AiService ai) {
        this.ai = ai;
    }

    @PostMapping("/explain")
    public AiGenerationResponse explain(@AuthenticationPrincipal AccountPrincipal principal,
                                        @Valid @RequestBody AiRequest request) {
        return ai.explain(principal.id(), request);
    }

    @PostMapping("/generate-exercise")
    public AiGenerationResponse generateExercise(@AuthenticationPrincipal AccountPrincipal principal,
                                                 @Valid @RequestBody AiRequest request) {
        return ai.generateExercise(principal.id(), request);
    }

    @PostMapping("/enrich-note")
    public AiGenerationResponse enrichNote(@AuthenticationPrincipal AccountPrincipal principal,
                                          @Valid @RequestBody AiRequest request) {
        return ai.enrichNote(principal.id(), request);
    }
}