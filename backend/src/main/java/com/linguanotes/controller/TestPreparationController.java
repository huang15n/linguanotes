package com.linguanotes.controller;

import com.linguanotes.dto.TestPreparationAutosaveRequest;
import com.linguanotes.dto.TestPreparationRequest;
import com.linguanotes.dto.TestPreparationResponse;
import com.linguanotes.model.AccountRole;
import com.linguanotes.model.ExamLanguage;
import com.linguanotes.model.ExamSkillModule;
import com.linguanotes.model.ExamType;
import com.linguanotes.model.PublicationStatus;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.TestPreparationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test-preparations")
public class TestPreparationController {

    private final TestPreparationService service;

    public TestPreparationController(TestPreparationService service) {
        this.service = service;
    }

    private static boolean isAdmin(AccountPrincipal principal) {
        return principal.role() == AccountRole.ADMIN;
    }

    @GetMapping
    public List<TestPreparationResponse> search(
            @AuthenticationPrincipal AccountPrincipal principal,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ExamLanguage examLanguage,
            @RequestParam(required = false) ExamType examType,
            @RequestParam(required = false) String examLevel,
            @RequestParam(required = false) ExamSkillModule skillModule,
            @RequestParam(required = false) Boolean favorite,
            @RequestParam(required = false) PublicationStatus status,
            @RequestParam(required = false, defaultValue = "false") boolean mineOnly) {
        return service.search(principal.id(), isAdmin(principal), q, examLanguage, examType, examLevel,
                skillModule, favorite, status, mineOnly);
    }

    @GetMapping("/{id}")
    public TestPreparationResponse get(@PathVariable Long id,
                                       @AuthenticationPrincipal AccountPrincipal principal) {
        return service.get(id, principal.id(), isAdmin(principal));
    }

    @PostMapping
    public ResponseEntity<TestPreparationResponse> create(
            @Valid @RequestBody TestPreparationRequest request,
            @AuthenticationPrincipal AccountPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, principal.id()));
    }

    @PutMapping("/{id}")
    public TestPreparationResponse update(
            @PathVariable Long id,
            @Valid @RequestBody TestPreparationRequest request,
            @AuthenticationPrincipal AccountPrincipal principal) {
        return service.update(id, request, principal.id(), isAdmin(principal));
    }

    @PatchMapping("/{id}/autosave")
    public TestPreparationResponse autosave(
            @PathVariable Long id,
            @Valid @RequestBody TestPreparationAutosaveRequest request,
            @AuthenticationPrincipal AccountPrincipal principal) {
        return service.autosave(id, request, principal.id(), isAdmin(principal));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @AuthenticationPrincipal AccountPrincipal principal) {
        service.delete(id, principal.id(), isAdmin(principal));
    }
}
