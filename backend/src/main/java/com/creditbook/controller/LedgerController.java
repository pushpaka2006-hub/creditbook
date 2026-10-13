package com.creditbook.controller;

import com.creditbook.dto.LedgerResponse;
import com.creditbook.security.UserPrincipal;
import com.creditbook.service.LedgerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/customers/{customerId}/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping
    public ResponseEntity<LedgerResponse> getCustomerLedger(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        LedgerResponse ledger = ledgerService.getCustomerLedger(currentUser.getId(), customerId, startDate, endDate);
        return ResponseEntity.ok(ledger);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportLedgerCsv(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        String csvContent = ledgerService.generateLedgerCsv(currentUser.getId(), customerId, startDate, endDate);
        byte[] csvBytes = csvContent.getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "customer_" + customerId + "_ledger.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }
}
