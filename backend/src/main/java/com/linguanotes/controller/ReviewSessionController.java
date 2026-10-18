package com.linguanotes.controller;

import com.linguanotes.dto.ReviewSessionRequest;
import com.linguanotes.dto.ReviewSessionResponse;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.ReviewSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review/sessions")
public class ReviewSessionController {
    private final ReviewSessionService sessions;

    public ReviewSessionController(ReviewSessionService sessions) {
        this.sessions = sessions;
    }

    @GetMapping
    public List<ReviewSessionResponse> list(@AuthenticationPrincipal AccountPrincipal principal) {
        return sessions.list(principal.id());
    }

    @PostMapping
    public ResponseEntity<ReviewSessionResponse> create(@AuthenticationPrincipal AccountPrincipal principal,
                                                        @Valid @RequestBody ReviewSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sessions.create(principal.id(), request));
    }
}
