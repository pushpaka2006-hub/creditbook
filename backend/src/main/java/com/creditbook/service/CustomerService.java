package com.creditbook.service;

import com.creditbook.dto.CustomerRequest;
import com.creditbook.dto.CustomerResponse;
import com.creditbook.entity.Customer;
import com.creditbook.entity.CustomerStatus;
import com.creditbook.entity.Transaction;
import com.creditbook.entity.TransactionType;
import com.creditbook.entity.User;
import com.creditbook.exception.BadRequestException;
import com.creditbook.exception.ResourceNotFoundException;
import com.creditbook.repository.CustomerRepository;
import com.creditbook.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final AuthService authService;
    private final AuditLogService auditLogService;

    public CustomerService(CustomerRepository customerRepository,
                           TransactionRepository transactionRepository,
                           AuthService authService,
                           AuditLogService auditLogService) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.authService = authService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public CustomerResponse createCustomer(Long userId, CustomerRequest request, String ipAddress) {
        User user = authService.getUserById(userId);

        String trimmedMobile = request.getMobile().trim();
        if (customerRepository.existsByUserIdAndMobile(userId, trimmedMobile)) {
            throw new BadRequestException("A customer with mobile number " + trimmedMobile + " already exists in your credit book");
        }

        Customer customer = new Customer(
                user,
                request.getFullName().trim(),
                trimmedMobile,
                request.getEmail() != null ? request.getEmail().trim() : null,
                request.getAddress() != null ? request.getAddress().trim() : null,
                request.getNotes() != null ? request.getNotes().trim() : null,
                request.getOpeningBalance() != null ? request.getOpeningBalance() : BigDecimal.ZERO
        );

        if (request.getStatus() != null) {
            customer.setStatus(request.getStatus());
        }

        Customer savedCustomer = customerRepository.save(customer);

        auditLogService.recordLog(user, "CREATE_CUSTOMER", "CUSTOMER", savedCustomer.getId(),
                "Created customer: " + savedCustomer.getFullName() + " (" + savedCustomer.getMobile() + ")", ipAddress);

        return mapToResponse(savedCustomer, userId);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(Long userId, Long customerId) {
        Customer customer = customerRepository.findByUserIdAndId(userId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
        return mapToResponse(customer, userId);
    }

    @Transactional
    public CustomerResponse updateCustomer(Long userId, Long customerId, CustomerRequest request, String ipAddress) {
        User user = authService.getUserById(userId);
        Customer customer = customerRepository.findByUserIdAndId(userId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        String trimmedMobile = request.getMobile().trim();
        if (customerRepository.existsByUserIdAndMobileAndIdNot(userId, trimmedMobile, customerId)) {
            throw new BadRequestException("Another customer with mobile number " + trimmedMobile + " already exists in your credit book");
        }

        customer.setFullName(request.getFullName().trim());
        customer.setMobile(trimmedMobile);
        customer.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        customer.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
        customer.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);
        if (request.getStatus() != null) {
            customer.setStatus(request.getStatus());
        }
        if (request.getOpeningBalance() != null) {
            customer.setOpeningBalance(request.getOpeningBalance());
        }

        Customer updatedCustomer = customerRepository.save(customer);

        auditLogService.recordLog(user, "UPDATE_CUSTOMER", "CUSTOMER", updatedCustomer.getId(),
                "Updated details for customer: " + updatedCustomer.getFullName(), ipAddress);

        return mapToResponse(updatedCustomer, userId);
    }

    @Transactional
    public void deleteCustomer(Long userId, Long customerId, String ipAddress) {
        User user = authService.getUserById(userId);
        Customer customer = customerRepository.findByUserIdAndId(userId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        // Protection check: Cannot delete customer with non-reversed transactions
        long txCount = transactionRepository.countByUserIdAndCustomerIdAndReversedFalse(userId, customerId);
        if (txCount > 0) {
            throw new BadRequestException("Cannot delete customer '" + customer.getFullName() +
                    "' because they have " + txCount + " active transaction record(s). Reverse or settle them first.");
        }

        customerRepository.delete(customer);

        auditLogService.recordLog(user, "DELETE_CUSTOMER", "CUSTOMER", customerId,
                "Deleted customer: " + customer.getFullName(), ipAddress);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> listCustomers(Long userId, CustomerStatus status, String search) {
        List<Customer> customers;
        if ((search != null && !search.trim().isEmpty()) || status != null) {
            customers = customerRepository.filterCustomers(userId, status, search != null ? search.trim() : null);
        } else {
            customers = customerRepository.findByUserIdOrderByFullNameAsc(userId);
        }

        return customers.stream()
                .map(customer -> mapToResponse(customer, userId))
                .collect(Collectors.toList());
    }

    public Customer getCustomerEntity(Long userId, Long customerId) {
        return customerRepository.findByUserIdAndId(userId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
    }

    public CustomerResponse mapToResponse(Customer customer, Long userId) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setFullName(customer.getFullName());
        response.setMobile(customer.getMobile());
        response.setEmail(customer.getEmail());
        response.setAddress(customer.getAddress());
        response.setNotes(customer.getNotes());
        response.setStatus(customer.getStatus());
        response.setOpeningBalance(customer.getOpeningBalance() != null ? customer.getOpeningBalance() : BigDecimal.ZERO);
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());

        // Calculate accurate financial totals for this customer
        BigDecimal creditGiven = transactionRepository.sumAmountByUserIdAndCustomerIdAndType(userId, customer.getId(), TransactionType.CREDIT_GIVEN);
        BigDecimal paymentReceived = transactionRepository.sumAmountByUserIdAndCustomerIdAndType(userId, customer.getId(), TransactionType.PAYMENT_RECEIVED);
        BigDecimal creditBorrowed = transactionRepository.sumAmountByUserIdAndCustomerIdAndType(userId, customer.getId(), TransactionType.CREDIT_BORROWED);
        BigDecimal paymentMade = transactionRepository.sumAmountByUserIdAndCustomerIdAndType(userId, customer.getId(), TransactionType.PAYMENT_MADE);

        creditGiven = creditGiven != null ? creditGiven : BigDecimal.ZERO;
        paymentReceived = paymentReceived != null ? paymentReceived : BigDecimal.ZERO;
        creditBorrowed = creditBorrowed != null ? creditBorrowed : BigDecimal.ZERO;
        paymentMade = paymentMade != null ? paymentMade : BigDecimal.ZERO;

        response.setTotalCreditGiven(creditGiven);
        response.setTotalPaymentReceived(paymentReceived);

        // Outstanding Balance = Opening Balance + Total Credit Given - Total Payments Received
        BigDecimal outstanding = response.getOpeningBalance().add(creditGiven).subtract(paymentReceived);
        response.setOutstandingBalance(outstanding);

        // Borrowed balances
        response.setTotalBorrowed(creditBorrowed);
        response.setTotalRepaid(paymentMade);
        response.setBorrowedBalance(creditBorrowed.subtract(paymentMade));

        // Latest transaction date
        Optional<Transaction> latestTx = transactionRepository.findFirstByUserIdAndCustomerIdOrderByTransactionDateDesc(userId, customer.getId());
        response.setLastTransactionDate(latestTx.map(Transaction::getTransactionDate).orElse(null));

        return response;
    }
}
