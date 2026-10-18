package com.linguanotes.controller;

import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.NoteService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    private final NoteService notes;

    public TagController(NoteService notes) {
        this.notes = notes;
    }

    @GetMapping
    public List<String> tags(@AuthenticationPrincipal AccountPrincipal principal) {
        return notes.allTags(principal.id());
    }
}
