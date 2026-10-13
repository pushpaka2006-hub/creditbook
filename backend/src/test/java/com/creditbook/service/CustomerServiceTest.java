package com.creditbook.service;

import com.creditbook.dto.CustomerRequest;
import com.creditbook.dto.CustomerResponse;
import com.creditbook.entity.Customer;
import com.creditbook.entity.CustomerStatus;
import com.creditbook.entity.User;
import com.creditbook.exception.BadRequestException;
import com.creditbook.exception.ResourceNotFoundException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Service Unit Tests")
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuthService authService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private CustomerService customerService;

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
    @DisplayName("Successfully create a new customer")
    void testCreateCustomerSuccess() {
        CustomerRequest request = new CustomerRequest("Ramesh Patel", "9822011223", "ramesh@example.com", "Pune", "Notes", CustomerStatus.ACTIVE, BigDecimal.ZERO);

        when(authService.getUserById(1L)).thenReturn(testUser);
        when(customerRepository.existsByUserIdAndMobile(1L, "9822011223")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        CustomerResponse response = customerService.createCustomer(1L, request, "127.0.0.1");

        assertNotNull(response);
        assertEquals("Ramesh Patel", response.getFullName());
        assertEquals("9822011223", response.getMobile());
        verify(customerRepository).save(any(Customer.class));
        verify(auditLogService).recordLog(eq(testUser), eq("CREATE_CUSTOMER"), eq("CUSTOMER"), anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("Reject customer creation with duplicate mobile number for same user")
    void testCreateCustomerDuplicateMobile() {
        CustomerRequest request = new CustomerRequest("Duplicate Person", "9822011223", null, null, null, CustomerStatus.ACTIVE, BigDecimal.ZERO);

        when(authService.getUserById(1L)).thenReturn(testUser);
        when(customerRepository.existsByUserIdAndMobile(1L, "9822011223")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> customerService.createCustomer(1L, request, "127.0.0.1"));
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Prevent deleting customer when active transaction history exists")
    void testDeleteCustomerWithActiveTransactionsFails() {
        when(authService.getUserById(1L)).thenReturn(testUser);
        when(customerRepository.findByUserIdAndId(1L, 10L)).thenReturn(Optional.of(testCustomer));
        when(transactionRepository.countByUserIdAndCustomerIdAndReversedFalse(1L, 10L)).thenReturn(3L);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                customerService.deleteCustomer(1L, 10L, "127.0.0.1"));

        assertTrue(ex.getMessage().contains("active transaction record"));
        verify(customerRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Successfully delete customer when no active transactions exist")
    void testDeleteCustomerWithoutTransactionsSuccess() {
        when(authService.getUserById(1L)).thenReturn(testUser);
        when(customerRepository.findByUserIdAndId(1L, 10L)).thenReturn(Optional.of(testCustomer));
        when(transactionRepository.countByUserIdAndCustomerIdAndReversedFalse(1L, 10L)).thenReturn(0L);

        customerService.deleteCustomer(1L, 10L, "127.0.0.1");

        verify(customerRepository).delete(testCustomer);
        verify(auditLogService).recordLog(eq(testUser), eq("DELETE_CUSTOMER"), eq("CUSTOMER"), eq(10L), anyString(), anyString());
    }
}
