package com.linguanotes.controller;

import com.linguanotes.dto.Dto.DashboardResponse;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.NoteService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api")
public class MiscController {
    private final NoteService service;
    public MiscController(NoteService service) { this.service = service; }

    @GetMapping("/dashboard") public DashboardResponse dashboard(@AuthenticationPrincipal AccountPrincipal principal) { return service.dashboard(principal.id()); }
    @GetMapping("/tags") public List<String> tags(@AuthenticationPrincipal AccountPrincipal principal) { return service.allTags(principal.id()); }
}
