package com.linguanotes.controller;

import com.linguanotes.dto.ClassMemberResponse;
import com.linguanotes.dto.ClassRequest;
import com.linguanotes.dto.ClassResponse;
import com.linguanotes.dto.JoinClassRequest;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.ClassService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class ClassController {
    private final ClassService classes;

    public ClassController(ClassService classes) {
        this.classes = classes;
    }

    @GetMapping
    public List<ClassResponse> list(@AuthenticationPrincipal AccountPrincipal principal) {
        return classes.list(principal.id());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<ClassResponse> create(@AuthenticationPrincipal AccountPrincipal principal,
                                                @Valid @RequestBody ClassRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classes.create(principal.id(), request));
    }

    @PostMapping("/join")
    public ClassResponse join(@AuthenticationPrincipal AccountPrincipal principal,
                              @Valid @RequestBody JoinClassRequest request) {
        return classes.join(principal.id(), request.joinCode());
    }

    @GetMapping("/{id}/members")
    public List<ClassMemberResponse> members(@AuthenticationPrincipal AccountPrincipal principal,
                                             @PathVariable Long id) {
        return classes.members(id, principal.id());
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<Void> removeMember(@AuthenticationPrincipal AccountPrincipal principal,
                                             @PathVariable Long id, @PathVariable Long memberId) {
        classes.removeMember(id, memberId, principal.id());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AccountPrincipal principal,
                                       @PathVariable Long id) {
        classes.delete(id, principal.id());
        return ResponseEntity.noContent().build();
    }
}
