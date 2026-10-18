package com.linguanotes.controller;

import com.linguanotes.dto.Dto.*;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.ExerciseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {
    private final ExerciseService exercises;

    public ExerciseController(ExerciseService exercises) {
        this.exercises = exercises;
    }

    @GetMapping
    public List<ExerciseResponse> list(@AuthenticationPrincipal AccountPrincipal principal) {
        return exercises.list(principal.id());
    }

    @PostMapping
    public ResponseEntity<ExerciseResponse> create(@AuthenticationPrincipal AccountPrincipal principal,
                                                   @Valid @RequestBody ExerciseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exercises.create(principal.id(), request));
    }

    @GetMapping("/{id}")
    public ExerciseResponse get(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long id) {
        return exercises.get(principal.id(), id);
    }

    @GetMapping("/{id}/attempts")
    public List<ExerciseAttemptSummary> attempts(@AuthenticationPrincipal AccountPrincipal principal,
                                                 @PathVariable Long id) {
        return exercises.attempts(principal.id(), id);
    }

    @PostMapping("/{id}/attempts")
    public ExerciseAttemptResponse submit(@AuthenticationPrincipal AccountPrincipal principal,
                                          @PathVariable Long id, @Valid @RequestBody ExerciseAttemptRequest request) {
        return exercises.submit(principal.id(), id, request);
    }
}