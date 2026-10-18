package com.linguanotes.controller;

import com.linguanotes.dto.LessonSectionRequest;
import com.linguanotes.dto.LessonSectionResponse;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.LessonSectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons/{lessonId}/sections")
public class LessonSectionController {
    private final LessonSectionService sections;

    public LessonSectionController(LessonSectionService sections) {
        this.sections = sections;
    }

    @GetMapping
    public List<LessonSectionResponse> list(@AuthenticationPrincipal AccountPrincipal principal,
                                            @PathVariable Long lessonId) {
        return sections.list(principal.id(), lessonId);
    }

    @PostMapping
    public ResponseEntity<LessonSectionResponse> create(@AuthenticationPrincipal AccountPrincipal principal,
                                                        @PathVariable Long lessonId,
                                                        @Valid @RequestBody LessonSectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sections.create(principal.id(), lessonId, request));
    }

    @PutMapping("/{sectionId}")
    public LessonSectionResponse update(@AuthenticationPrincipal AccountPrincipal principal,
                                        @PathVariable Long lessonId, @PathVariable Long sectionId,
                                        @Valid @RequestBody LessonSectionRequest request) {
        return sections.update(principal.id(), lessonId, sectionId, request);
    }

    @DeleteMapping("/{sectionId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AccountPrincipal principal,
                                       @PathVariable Long lessonId, @PathVariable Long sectionId) {
        sections.delete(principal.id(), lessonId, sectionId);
        return ResponseEntity.noContent().build();
    }
}
