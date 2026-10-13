package com.creditbook.service;

import com.creditbook.dto.CustomerResponse;
import com.creditbook.dto.TransactionRequest;
import com.creditbook.dto.TransactionResponse;
import com.creditbook.dto.TransactionUpdateDto;
import com.creditbook.entity.*;
import com.creditbook.exception.BadRequestException;
import com.creditbook.exception.ResourceNotFoundException;
import com.creditbook.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CustomerService customerService;
    private final AuthService authService;
    private final AuditLogService auditLogService;

    public TransactionService(TransactionRepository transactionRepository,
                              CustomerService customerService,
                              AuthService authService,
                              AuditLogService auditLogService) {
        this.transactionRepository = transactionRepository;
        this.customerService = customerService;
        this.authService = authService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public TransactionResponse createTransaction(Long userId, TransactionRequest request, String ipAddress) {
        User user = authService.getUserById(userId);
        Customer customer = customerService.getCustomerEntity(userId, request.getCustomerId());

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Transaction amount must be strictly greater than zero");
        }

        // Section 7 Validation: Prevent payments that exceed outstanding debt unless noted
        if (request.getTransactionType() == TransactionType.PAYMENT_RECEIVED) {
            CustomerResponse customerSummary = customerService.getCustomer(userId, customer.getId());
            BigDecimal currentDue = customerSummary.getOutstandingBalance();
            if (currentDue.compareTo(BigDecimal.ZERO) <= 0) {
                // If customer has zero balance or credit, check if user specifically entered notes
                if (request.getDescription() == null || !request.getDescription().toLowerCase().contains("advance")) {
                    throw new BadRequestException("Customer has no outstanding balance (current due is ₹" +
                            currentDue + "). To record an advance deposit, specify 'Advance' in description.");
                }
            } else if (amount.compareTo(currentDue) > 0) {
                if (request.getDescription() == null || !request.getDescription().toLowerCase().contains("advance")) {
                    throw new BadRequestException("Payment amount of ₹" + amount.toPlainString() +
                            " exceeds customer's outstanding balance of ₹" + currentDue.toPlainString() +
                            ". To allow excess as advance credit, include 'Advance' in notes.");
                }
            }
        } else if (request.getTransactionType() == TransactionType.PAYMENT_MADE) {
            CustomerResponse customerSummary = customerService.getCustomer(userId, customer.getId());
            BigDecimal currentBorrowed = customerSummary.getBorrowedBalance();
            if (amount.compareTo(currentBorrowed) > 0 && currentBorrowed.compareTo(BigDecimal.ZERO) > 0) {
                throw new BadRequestException("Repayment amount of ₹" + amount.toPlainString() +
                        " exceeds current borrowed amount of ₹" + currentBorrowed.toPlainString());
            }
        }

        Transaction transaction = new Transaction(
                user,
                customer,
                request.getTransactionType(),
                amount,
                request.getTransactionDate() != null ? request.getTransactionDate() : LocalDateTime.now(),
                request.getDescription(),
                request.getPaymentMethod(),
                request.getReferenceNumber()
        );

        Transaction savedTx = transactionRepository.save(transaction);

        auditLogService.recordLog(user, "CREATE_TRANSACTION", "TRANSACTION", savedTx.getId(),
                "Recorded " + savedTx.getTransactionType() + " of ₹" + savedTx.getAmount() +
                " for customer " + customer.getFullName(), ipAddress);

        return mapToResponse(savedTx);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(Long userId, Long transactionId) {
        Transaction tx = transactionRepository.findByUserIdAndId(userId, transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));
        return mapToResponse(tx);
    }

    @Transactional
    public TransactionResponse updateTransaction(Long userId, Long transactionId, TransactionUpdateDto updateDto, String ipAddress) {
        User user = authService.getUserById(userId);
        Transaction tx = transactionRepository.findByUserIdAndId(userId, transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        if (tx.isReversed()) {
            throw new BadRequestException("Cannot edit a reversed transaction");
        }

        if (updateDto.getDescription() != null) {
            tx.setDescription(updateDto.getDescription().trim());
        }
        if (updateDto.getPaymentMethod() != null) {
            tx.setPaymentMethod(updateDto.getPaymentMethod());
        }
        if (updateDto.getReferenceNumber() != null) {
            tx.setReferenceNumber(updateDto.getReferenceNumber().trim());
        }
        if (updateDto.getTransactionDate() != null) {
            tx.setTransactionDate(updateDto.getTransactionDate());
        }

        Transaction updatedTx = transactionRepository.save(tx);

        auditLogService.recordLog(user, "UPDATE_TRANSACTION", "TRANSACTION", updatedTx.getId(),
                "Updated details of transaction #" + updatedTx.getId(), ipAddress);

        return mapToResponse(updatedTx);
    }

    @Transactional
    public TransactionResponse reverseTransaction(Long userId, Long transactionId, String reason, String ipAddress) {
        User user = authService.getUserById(userId);
        Transaction tx = transactionRepository.findByUserIdAndId(userId, transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        if (tx.isReversed()) {
            throw new BadRequestException("Transaction #" + transactionId + " is already reversed");
        }

        tx.setReversed(true);
        String reversalNote = "[REVERSED: " + (reason != null && !reason.trim().isEmpty() ? reason.trim() : "Accidental Entry") + "] " +
                (tx.getDescription() != null ? tx.getDescription() : "");
        tx.setDescription(reversalNote);

        Transaction reversedTx = transactionRepository.save(tx);

        auditLogService.recordLog(user, "REVERSE_TRANSACTION", "TRANSACTION", reversedTx.getId(),
                "Reversed transaction #" + reversedTx.getId() + " (" + reversedTx.getAmount() + ") Reason: " + reason, ipAddress);

        return mapToResponse(reversedTx);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> listTransactions(Long userId, Long customerId, TransactionType type,
                                                     LocalDateTime startDate, LocalDateTime endDate, String query) {
        List<Transaction> transactions = transactionRepository.filterTransactions(
                userId, customerId, type, startDate, endDate, query != null ? query.trim() : null
        );

        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TransactionResponse mapToResponse(Transaction tx) {
        TransactionResponse response = new TransactionResponse();
        response.setId(tx.getId());
        response.setCustomerId(tx.getCustomer().getId());
        response.setCustomerName(tx.getCustomer().getFullName());
        response.setCustomerMobile(tx.getCustomer().getMobile());
        response.setTransactionType(tx.getTransactionType());
        response.setAmount(tx.getAmount());
        response.setTransactionDate(tx.getTransactionDate());
        response.setDescription(tx.getDescription());
        response.setPaymentMethod(tx.getPaymentMethod());
        response.setReferenceNumber(tx.getReferenceNumber());
        response.setReversed(tx.isReversed());
        response.setCreatedAt(tx.getCreatedAt());
        response.setUpdatedAt(tx.getUpdatedAt());

        // Dynamic payment status determination
        if (tx.isReversed()) {
            response.setPaymentStatus("REVERSED");
        } else if (tx.getTransactionType() == TransactionType.PAYMENT_RECEIVED || tx.getTransactionType() == TransactionType.PAYMENT_MADE) {
            response.setPaymentStatus("PAID");
        } else {
            response.setPaymentStatus("DUE");
        }

        return response;
    }
}
