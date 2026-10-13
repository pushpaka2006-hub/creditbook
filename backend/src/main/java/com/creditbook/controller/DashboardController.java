package com.creditbook.controller;

import com.creditbook.dto.DashboardSummaryDto;
import com.creditbook.security.UserPrincipal;
import com.creditbook.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary(@AuthenticationPrincipal UserPrincipal currentUser) {
        DashboardSummaryDto summary = dashboardService.getDashboardSummary(currentUser.getId());
        return ResponseEntity.ok(summary);
    }
}
