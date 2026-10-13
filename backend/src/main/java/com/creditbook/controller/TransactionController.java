package com.creditbook.controller;

import com.creditbook.dto.TransactionRequest;
import com.creditbook.dto.TransactionResponse;
import com.creditbook.dto.TransactionUpdateDto;
import com.creditbook.entity.TransactionType;
import com.creditbook.security.UserPrincipal;
import com.creditbook.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> listTransactions(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String query) {
        List<TransactionResponse> transactions = transactionService.listTransactions(
                currentUser.getId(), customerId, type, startDate, endDate, query
        );
        return ResponseEntity.ok(transactions);
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody TransactionRequest request,
            HttpServletRequest httpRequest) {
        TransactionResponse created = transactionService.createTransaction(
                currentUser.getId(), request, httpRequest.getRemoteAddr()
        );
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {
        TransactionResponse transaction = transactionService.getTransaction(currentUser.getId(), id);
        return ResponseEntity.ok(transaction);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            @RequestBody TransactionUpdateDto updateDto,
            HttpServletRequest httpRequest) {
        TransactionResponse updated = transactionService.updateTransaction(
                currentUser.getId(), id, updateDto, httpRequest.getRemoteAddr()
        );
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/reverse")
    public ResponseEntity<TransactionResponse> reverseTransaction(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest httpRequest) {
        String reason = body != null ? body.get("reason") : "User requested reversal";
        TransactionResponse reversed = transactionService.reverseTransaction(
                currentUser.getId(), id, reason, httpRequest.getRemoteAddr()
        );
        return ResponseEntity.ok(reversed);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTransaction(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        transactionService.reverseTransaction(currentUser.getId(), id, "Deleted by user", httpRequest.getRemoteAddr());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Transaction marked as reversed to protect ledger integrity");
        return ResponseEntity.ok(response);
    }
}
