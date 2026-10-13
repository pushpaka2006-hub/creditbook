package com.creditbook.service;

import com.creditbook.dto.CustomerResponse;
import com.creditbook.dto.TransactionRequest;
import com.creditbook.dto.TransactionResponse;
import com.creditbook.entity.*;
import com.creditbook.exception.BadRequestException;
import com.creditbook.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transaction Service Unit Tests")
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private AuthService authService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testUser = new User("Rajesh Sharma", "demo@creditbook.com", "hash", "Sharma Store", "9876543210");
        testUser.setId(1L);

        testCustomer = new Customer(testUser, "Ramesh Patel", "9822011223", "ramesh@example.com", "Pune", "Notes", BigDecimal.ZERO);
        testCustomer.setId(10L);
    }

    @Test
    @DisplayName("Reject transaction with non-positive amount")
    void testRejectZeroOrNegativeAmount() {
        TransactionRequest request = new TransactionRequest(10L, TransactionType.CREDIT_GIVEN, BigDecimal.ZERO, null, "Test", PaymentMethod.CASH, null);

        when(authService.getUserById(1L)).thenReturn(testUser);
        when(customerService.getCustomerEntity(1L, 10L)).thenReturn(testCustomer);

        assertThrows(BadRequestException.class, () ->
                transactionService.createTransaction(1L, request, "127.0.0.1"));
    }

    @Test
    @DisplayName("Reject payment exceeding outstanding balance unless marked advance")
    void testRejectPaymentExceedingOutstandingBalance() {
        TransactionRequest request = new TransactionRequest(10L, TransactionType.PAYMENT_RECEIVED, new BigDecimal("6000.00"), null, "Repayment", PaymentMethod.CASH, null);

        CustomerResponse customerSummary = new CustomerResponse();
        customerSummary.setOutstandingBalance(new BigDecimal("5000.00")); // Only 5000 due

        when(authService.getUserById(1L)).thenReturn(testUser);
        when(customerService.getCustomerEntity(1L, 10L)).thenReturn(testCustomer);
        when(customerService.getCustomer(1L, 10L)).thenReturn(customerSummary);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                transactionService.createTransaction(1L, request, "127.0.0.1"));

        assertTrue(ex.getMessage().contains("exceeds customer's outstanding balance"));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Allow payment when amount equals outstanding balance")
    void testAllowExactFullSettlement() {
        TransactionRequest request = new TransactionRequest(10L, TransactionType.PAYMENT_RECEIVED, new BigDecimal("5000.00"), null, "Full Settlement", PaymentMethod.UPI, "UPI-1234");

        CustomerResponse customerSummary = new CustomerResponse();
        customerSummary.setOutstandingBalance(new BigDecimal("5000.00"));

        Transaction savedTx = new Transaction(testUser, testCustomer, TransactionType.PAYMENT_RECEIVED, new BigDecimal("5000.00"), LocalDateTime.now(), "Full Settlement", PaymentMethod.UPI, "UPI-1234");
        savedTx.setId(50L);

        when(authService.getUserById(1L)).thenReturn(testUser);
        when(customerService.getCustomerEntity(1L, 10L)).thenReturn(testCustomer);
        when(customerService.getCustomer(1L, 10L)).thenReturn(customerSummary);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        TransactionResponse response = transactionService.createTransaction(1L, request, "127.0.0.1");

        assertNotNull(response);
        assertEquals(new BigDecimal("5000.00"), response.getAmount());
        assertEquals("PAID", response.getPaymentStatus());
        verify(auditLogService).recordLog(eq(testUser), eq("CREATE_TRANSACTION"), eq("TRANSACTION"), eq(50L), anyString(), anyString());
    }

    @Test
    @DisplayName("Safely reverse transaction and create audit log")
    void testReverseTransactionSuccess() {
        Transaction tx = new Transaction(testUser, testCustomer, TransactionType.CREDIT_GIVEN, new BigDecimal("2000.00"), LocalDateTime.now(), "Grocery purchase", PaymentMethod.CASH, null);
        tx.setId(77L);
        tx.setReversed(false);

        when(authService.getUserById(1L)).thenReturn(testUser);
        when(transactionRepository.findByUserIdAndId(1L, 77L)).thenReturn(Optional.of(tx));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(tx);

        TransactionResponse response = transactionService.reverseTransaction(1L, 77L, "Entry error by cashier", "127.0.0.1");

        assertNotNull(response);
        assertTrue(response.isReversed());
        assertEquals("REVERSED", response.getPaymentStatus());
        verify(auditLogService).recordLog(eq(testUser), eq("REVERSE_TRANSACTION"), eq("TRANSACTION"), eq(77L), anyString(), anyString());
    }
}
