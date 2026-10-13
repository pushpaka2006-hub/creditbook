package com.creditbook.service;

import com.creditbook.dto.CustomerResponse;
import com.creditbook.dto.PendingPaymentDto;
import com.creditbook.entity.Customer;
import com.creditbook.repository.CustomerRepository;
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
@DisplayName("Report Service Unit Tests")
public class ReportServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private ReportService reportService;

    private Customer c1;
    private Customer c2;

    @BeforeEach
    void setUp() {
        c1 = new Customer(null, "Ramesh Patel", "9822011223", "ramesh@example.com", "Pune", null, BigDecimal.ZERO);
        c1.setId(1L);

        c2 = new Customer(null, "Amit Verma", "9988776655", "amit@example.com", "Pune", null, BigDecimal.ZERO);
        c2.setId(2L);
    }

    @Test
    @DisplayName("Verify pending payments sorting by balance descending")
    void testPendingPaymentsSortByBalance() {
        CustomerResponse r1 = new CustomerResponse();
        r1.setId(1L);
        r1.setOutstandingBalance(new BigDecimal("3000.00"));
        r1.setLastTransactionDate(LocalDateTime.now().minusDays(5));

        CustomerResponse r2 = new CustomerResponse();
        r2.setId(2L);
        r2.setOutstandingBalance(new BigDecimal("8000.00"));
        r2.setLastTransactionDate(LocalDateTime.now().minusDays(10));

        when(customerRepository.findByUserIdOrderByFullNameAsc(1L)).thenReturn(Arrays.asList(c1, c2));
        when(customerService.mapToResponse(c1, 1L)).thenReturn(r1);
        when(customerService.mapToResponse(c2, 1L)).thenReturn(r2);

        List<PendingPaymentDto> pending = reportService.getPendingPayments(1L, "balance");

        assertNotNull(pending);
        assertEquals(2, pending.size());
        // Highest balance (8000) should be first
        assertEquals("Amit Verma", pending.get(0).getCustomerName());
        assertEquals(new BigDecimal("8000.00"), pending.get(0).getOutstandingBalance());
        assertEquals("Ramesh Patel", pending.get(1).getCustomerName());
        assertEquals(new BigDecimal("3000.00"), pending.get(1).getOutstandingBalance());

        // Verify reminder notice template is formatted accurately
        assertTrue(pending.get(0).getReminderMessage().contains("Amit Verma"));
        assertTrue(pending.get(0).getReminderMessage().contains("8000.00"));
    }

    @Test
    @DisplayName("Verify pending payments sorting by oldest transaction date")
    void testPendingPaymentsSortByOldestDate() {
        CustomerResponse r1 = new CustomerResponse();
        r1.setId(1L);
        r1.setOutstandingBalance(new BigDecimal("3000.00"));
        r1.setLastTransactionDate(LocalDateTime.now().minusDays(20)); // Older

        CustomerResponse r2 = new CustomerResponse();
        r2.setId(2L);
        r2.setOutstandingBalance(new BigDecimal("8000.00"));
        r2.setLastTransactionDate(LocalDateTime.now().minusDays(2));  // Newer

        when(customerRepository.findByUserIdOrderByFullNameAsc(1L)).thenReturn(Arrays.asList(c1, c2));
        when(customerService.mapToResponse(c1, 1L)).thenReturn(r1);
        when(customerService.mapToResponse(c2, 1L)).thenReturn(r2);

        List<PendingPaymentDto> pending = reportService.getPendingPayments(1L, "oldest");

        assertNotNull(pending);
        assertEquals(2, pending.size());
        // Oldest transaction (20 days ago) should be first
        assertEquals("Ramesh Patel", pending.get(0).getCustomerName());
        assertEquals("Amit Verma", pending.get(1).getCustomerName());
    }
}
