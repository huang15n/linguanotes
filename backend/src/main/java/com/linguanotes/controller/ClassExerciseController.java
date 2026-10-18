package com.linguanotes.controller;

import com.linguanotes.dto.ClassExerciseRequest;
import com.linguanotes.dto.ClassExerciseResponse;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.ClassExerciseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes/{classId}/exercises")
public class ClassExerciseController {
    private final ClassExerciseService assignments;

    public ClassExerciseController(ClassExerciseService assignments) {
        this.assignments = assignments;
    }

    @GetMapping
    public List<ClassExerciseResponse> list(@AuthenticationPrincipal AccountPrincipal principal,
                                            @PathVariable Long classId) {
        return assignments.list(classId, principal.id());
    }

    @PostMapping
    public ResponseEntity<ClassExerciseResponse> assign(@AuthenticationPrincipal AccountPrincipal principal,
                                                        @PathVariable Long classId,
                                                        @Valid @RequestBody ClassExerciseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assignments.assign(classId, principal.id(), request));
    }

    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal AccountPrincipal principal,
                                       @PathVariable Long classId, @PathVariable Long assignmentId) {
        assignments.remove(classId, assignmentId, principal.id());
        return ResponseEntity.noContent().build();
    }
}
