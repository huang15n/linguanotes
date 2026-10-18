package com.linguanotes.controller;

import com.linguanotes.dto.Dto.DashboardResponse;
import com.linguanotes.service.NoteService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api")
public class MiscController {
    private final NoteService service;
    public MiscController(NoteService service) { this.service = service; }

    @GetMapping("/dashboard") public DashboardResponse dashboard() { return service.dashboard(); }
    @GetMapping("/tags") public List<String> tags() { return service.allTags(); }
}
