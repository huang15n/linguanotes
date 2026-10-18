package com.linguanotes.controller;

import com.linguanotes.dto.DashboardResponse;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.NoteService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final NoteService notes;

    public DashboardController(NoteService notes) {
        this.notes = notes;
    }

    @GetMapping
    public DashboardResponse dashboard(@AuthenticationPrincipal AccountPrincipal principal) {
        return notes.dashboard(principal.id());
    }
}
