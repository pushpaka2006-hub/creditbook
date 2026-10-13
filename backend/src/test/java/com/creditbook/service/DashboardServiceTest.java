package com.creditbook.service;

import com.creditbook.dto.CustomerResponse;
import com.creditbook.dto.DashboardSummaryDto;
import com.creditbook.entity.Customer;
import com.creditbook.entity.TransactionType;
import com.creditbook.entity.User;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Dashboard Service Unit Tests")
public class DashboardServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private DashboardService dashboardService;

    private User testUser;
    private Customer customer1;
    private Customer customer2;

    @BeforeEach
    void setUp() {
        testUser = new User("Rajesh Sharma", "demo@creditbook.com", "hash", "Sharma Store", "9876543210");
        testUser.setId(1L);

        customer1 = new Customer(testUser, "Ramesh Patel", "9822011223", "ramesh@example.com", "Pune", null, BigDecimal.ZERO);
        customer1.setId(10L);

        customer2 = new Customer(testUser, "Priya Sundaram", "9733445566", "priya@example.com", "Pune", null, BigDecimal.ZERO);
        customer2.setId(20L);
    }

    @Test
    @DisplayName("Verify dashboard aggregates statistics and ranks top debtors correctly")
    void testDashboardAggregationsAndDebtorRanking() {
        when(customerRepository.countByUserId(1L)).thenReturn(2L);
        when(transactionRepository.sumAmountByUserIdAndType(1L, TransactionType.CREDIT_GIVEN)).thenReturn(new BigDecimal("10000.00"));
        when(transactionRepository.sumAmountByUserIdAndType(1L, TransactionType.PAYMENT_RECEIVED)).thenReturn(new BigDecimal("4000.00"));
        when(transactionRepository.sumAmountByUserIdAndType(1L, TransactionType.CREDIT_BORROWED)).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumAmountByUserIdAndType(1L, TransactionType.PAYMENT_MADE)).thenReturn(BigDecimal.ZERO);

        when(customerRepository.findByUserIdOrderByFullNameAsc(1L)).thenReturn(Arrays.asList(customer1, customer2));

        CustomerResponse resp1 = new CustomerResponse();
        resp1.setId(10L);
        resp1.setFullName("Ramesh Patel");
        resp1.setOutstandingBalance(new BigDecimal("5000.00"));

        CustomerResponse resp2 = new CustomerResponse();
        resp2.setId(20L);
        resp2.setFullName("Priya Sundaram");
        resp2.setOutstandingBalance(new BigDecimal("1000.00"));

        when(customerService.mapToResponse(customer1, 1L)).thenReturn(resp1);
        when(customerService.mapToResponse(customer2, 1L)).thenReturn(resp2);

        when(transactionRepository.findTop10ByUserIdOrderByTransactionDateDesc(1L)).thenReturn(Collections.emptyList());

        DashboardSummaryDto summary = dashboardService.getDashboardSummary(1L);

        assertNotNull(summary);
        assertEquals(2, summary.getTotalCustomers());
        assertEquals(new BigDecimal("10000.00"), summary.getTotalMoneyLent());
        assertEquals(new BigDecimal("4000.00"), summary.getTotalPaymentsReceived());
        assertEquals(new BigDecimal("6000.00"), summary.getTotalOutstandingCredit()); // 5000 + 1000
        assertEquals(2, summary.getPendingPaymentCount());

        // Verify top debtors ranked highest due first
        assertEquals(2, summary.getTopDebtors().size());
        assertEquals("Ramesh Patel", summary.getTopDebtors().get(0).getFullName());
        assertEquals(new BigDecimal("5000.00"), summary.getTopDebtors().get(0).getOutstandingBalance());

        // Verify monthly chart generated 6 months of historical data points
        assertNotNull(summary.getMonthlyChart());
        assertEquals(6, summary.getMonthlyChart().size());
    }
}
