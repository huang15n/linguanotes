package com.linguanotes.controller;

import com.linguanotes.dto.DashboardResponse;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboard;

    public DashboardController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping
    public DashboardResponse dashboard(@AuthenticationPrincipal AccountPrincipal principal) {
        return dashboard.dashboard(principal.id());
    }
}
