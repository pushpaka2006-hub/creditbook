package com.creditbook.controller;

import com.creditbook.dto.PendingPaymentDto;
import com.creditbook.dto.ReportSummaryDto;
import com.creditbook.security.UserPrincipal;
import com.creditbook.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ReportSummaryDto> getReportSummary(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        ReportSummaryDto summary = reportService.getReportSummary(currentUser.getId(), startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PendingPaymentDto>> getPendingPayments(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false, defaultValue = "balance") String sortBy) {
        List<PendingPaymentDto> pending = reportService.getPendingPayments(currentUser.getId(), sortBy);
        return ResponseEntity.ok(pending);
    }

    @GetMapping("/export/transactions/csv")
    public ResponseEntity<byte[]> exportTransactionsCsv(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        String csv = reportService.generateTransactionsCsv(currentUser.getId(), startDate, endDate);
        byte[] bytes = csv.getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "creditbook_transactions.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }

    @GetMapping("/export/customers/csv")
    public ResponseEntity<byte[]> exportCustomersCsv(@AuthenticationPrincipal UserPrincipal currentUser) {
        String csv = reportService.generateCustomerReportCsv(currentUser.getId());
        byte[] bytes = csv.getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "creditbook_customers.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }
}
