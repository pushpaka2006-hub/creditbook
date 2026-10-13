package com.creditbook.controller;

import com.creditbook.dto.AuditLogResponse;
import com.creditbook.security.UserPrincipal;
import com.creditbook.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<AuditLogResponse> logs = auditLogService.getRecentLogs(currentUser.getId());
        return ResponseEntity.ok(logs);
    }
}
