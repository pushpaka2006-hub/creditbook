package com.creditbook.service;

import com.creditbook.dto.LedgerEntryDto;
import com.creditbook.dto.LedgerResponse;
import com.creditbook.entity.*;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Ledger Service Unit Tests")
public class LedgerServiceTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private LedgerService ledgerService;

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
    @DisplayName("Verify chronological running balance across multiple credit and payment events")
    void testChronologicalRunningBalanceCalculation() {
        LocalDateTime t1 = LocalDateTime.now().minusDays(20);
        LocalDateTime t2 = LocalDateTime.now().minusDays(15);
        LocalDateTime t3 = LocalDateTime.now().minusDays(10);
        LocalDateTime t4 = LocalDateTime.now().minusDays(5);

        // Transaction history:
        // 1. Credit 5,000 -> Running: 5,000
        // 2. Payment 1,500 -> Running: 3,500
        // 3. Credit 2,000 -> Running: 5,500
        // 4. Payment 500  -> Running: 5,000
        Transaction tx1 = new Transaction(testUser, testCustomer, TransactionType.CREDIT_GIVEN, new BigDecimal("5000.00"), t1, "Credit 1", PaymentMethod.CASH, null);
        tx1.setId(1L);

        Transaction tx2 = new Transaction(testUser, testCustomer, TransactionType.PAYMENT_RECEIVED, new BigDecimal("1500.00"), t2, "Payment 1", PaymentMethod.UPI, "UPI-001");
        tx2.setId(2L);

        Transaction tx3 = new Transaction(testUser, testCustomer, TransactionType.CREDIT_GIVEN, new BigDecimal("2000.00"), t3, "Credit 2", PaymentMethod.CASH, null);
        tx3.setId(3L);

        Transaction tx4 = new Transaction(testUser, testCustomer, TransactionType.PAYMENT_RECEIVED, new BigDecimal("500.00"), t4, "Payment 2", PaymentMethod.CASH, null);
        tx4.setId(4L);

        List<Transaction> txList = Arrays.asList(tx1, tx2, tx3, tx4);

        when(customerService.getCustomerEntity(1L, 10L)).thenReturn(testCustomer);
        when(transactionRepository.findByUserIdAndCustomerIdOrderByTransactionDateAsc(1L, 10L)).thenReturn(txList);

        LedgerResponse ledger = ledgerService.getCustomerLedger(1L, 10L, null, null);

        assertNotNull(ledger);
        assertEquals(4, ledger.getEntries().size());

        // Check running balances sequentially
        assertEquals(new BigDecimal("5000.00"), ledger.getEntries().get(0).getRunningBalance());
        assertEquals(new BigDecimal("3500.00"), ledger.getEntries().get(1).getRunningBalance());
        assertEquals(new BigDecimal("5500.00"), ledger.getEntries().get(2).getRunningBalance());
        assertEquals(new BigDecimal("5000.00"), ledger.getEntries().get(3).getRunningBalance());

        // Overall summary totals
        assertEquals(new BigDecimal("7000.00"), ledger.getTotalCredit());
        assertEquals(new BigDecimal("2000.00"), ledger.getTotalPaid());
        assertEquals(new BigDecimal("5000.00"), ledger.getCurrentOutstandingBalance());
    }

    @Test
    @DisplayName("Verify reversed transaction does not distort running balance")
    void testReversedTransactionIgnoredInRunningBalance() {
        Transaction tx1 = new Transaction(testUser, testCustomer, TransactionType.CREDIT_GIVEN, new BigDecimal("3000.00"), LocalDateTime.now().minusDays(5), "Valid Credit", PaymentMethod.CASH, null);
        tx1.setId(1L);

        Transaction tx2 = new Transaction(testUser, testCustomer, TransactionType.CREDIT_GIVEN, new BigDecimal("1000.00"), LocalDateTime.now().minusDays(3), "Mistake Entry", PaymentMethod.CASH, null);
        tx2.setId(2L);
        tx2.setReversed(true); // Marked reversed

        when(customerService.getCustomerEntity(1L, 10L)).thenReturn(testCustomer);
        when(transactionRepository.findByUserIdAndCustomerIdOrderByTransactionDateAsc(1L, 10L)).thenReturn(Arrays.asList(tx1, tx2));

        LedgerResponse ledger = ledgerService.getCustomerLedger(1L, 10L, null, null);

        assertNotNull(ledger);
        assertEquals(2, ledger.getEntries().size());
        assertEquals(new BigDecimal("3000.00"), ledger.getEntries().get(0).getRunningBalance());
        // Second entry was reversed, so running balance stays 3000.00
        assertEquals(new BigDecimal("3000.00"), ledger.getEntries().get(1).getRunningBalance());
        assertEquals(new BigDecimal("3000.00"), ledger.getCurrentOutstandingBalance());
    }

    @Test
    @DisplayName("Verify CSV ledger generation produces well-formed formatted output")
    void testCsvLedgerGeneration() {
        Transaction tx1 = new Transaction(testUser, testCustomer, TransactionType.CREDIT_GIVEN, new BigDecimal("2500.00"), LocalDateTime.now().minusDays(2), "Groceries", PaymentMethod.CASH, null);
        tx1.setId(1L);

        when(customerService.getCustomerEntity(1L, 10L)).thenReturn(testCustomer);
        when(transactionRepository.findByUserIdAndCustomerIdOrderByTransactionDateAsc(1L, 10L)).thenReturn(List.of(tx1));

        String csv = ledgerService.generateLedgerCsv(1L, 10L, null, null);

        assertNotNull(csv);
        assertTrue(csv.contains("Credit Book Ledger Report"));
        assertTrue(csv.contains("Ramesh Patel"));
        assertTrue(csv.contains("2500.00"));
        assertTrue(csv.contains("CREDIT_GIVEN"));
    }
}
