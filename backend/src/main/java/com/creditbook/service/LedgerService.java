package com.creditbook.service;

import com.creditbook.dto.LedgerEntryDto;
import com.creditbook.dto.LedgerResponse;
import com.creditbook.entity.Customer;
import com.creditbook.entity.Transaction;
import com.creditbook.entity.TransactionType;
import com.creditbook.repository.CustomerRepository;
import com.creditbook.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class LedgerService {

    private final CustomerService customerService;
    private final TransactionRepository transactionRepository;

    public LedgerService(CustomerService customerService,
                         TransactionRepository transactionRepository) {
        this.customerService = customerService;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public LedgerResponse getCustomerLedger(Long userId, Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        Customer customer = customerService.getCustomerEntity(userId, customerId);

        // Fetch all transactions chronologically (oldest to newest) to accurately calculate running balance
        List<Transaction> transactions = transactionRepository.findByUserIdAndCustomerIdOrderByTransactionDateAsc(userId, customerId);

        LedgerResponse ledger = new LedgerResponse();
        ledger.setCustomerId(customer.getId());
        ledger.setCustomerName(customer.getFullName());
        ledger.setCustomerMobile(customer.getMobile());
        ledger.setCustomerEmail(customer.getEmail());
        ledger.setCustomerAddress(customer.getAddress());

        BigDecimal openingBalance = customer.getOpeningBalance() != null ? customer.getOpeningBalance() : BigDecimal.ZERO;
        ledger.setOpeningBalance(openingBalance);

        BigDecimal runningBalance = openingBalance;
        BigDecimal runningBorrowedBalance = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalBorrowed = BigDecimal.ZERO;
        BigDecimal totalRepaid = BigDecimal.ZERO;

        List<LedgerEntryDto> entries = new ArrayList<>();

        for (Transaction tx : transactions) {
            LedgerEntryDto entry = new LedgerEntryDto();
            entry.setId(tx.getId());
            entry.setTransactionDate(tx.getTransactionDate());
            entry.setTransactionType(tx.getTransactionType());
            entry.setDescription(tx.getDescription());
            entry.setPaymentMethod(tx.getPaymentMethod());
            entry.setReferenceNumber(tx.getReferenceNumber());
            entry.setReversed(tx.isReversed());

            if (!tx.isReversed()) {
                if (tx.getTransactionType() == TransactionType.CREDIT_GIVEN) {
                    entry.setCreditAmount(tx.getAmount());
                    totalCredit = totalCredit.add(tx.getAmount());
                    runningBalance = runningBalance.add(tx.getAmount());
                } else if (tx.getTransactionType() == TransactionType.PAYMENT_RECEIVED) {
                    entry.setPaymentAmount(tx.getAmount());
                    totalPaid = totalPaid.add(tx.getAmount());
                    runningBalance = runningBalance.subtract(tx.getAmount());
                } else if (tx.getTransactionType() == TransactionType.CREDIT_BORROWED) {
                    entry.setBorrowedAmount(tx.getAmount());
                    totalBorrowed = totalBorrowed.add(tx.getAmount());
                    runningBorrowedBalance = runningBorrowedBalance.add(tx.getAmount());
                } else if (tx.getTransactionType() == TransactionType.PAYMENT_MADE) {
                    entry.setPaidBackAmount(tx.getAmount());
                    totalRepaid = totalRepaid.add(tx.getAmount());
                    runningBorrowedBalance = runningBorrowedBalance.subtract(tx.getAmount());
                }
            }

            entry.setRunningBalance(runningBalance);
            entry.setRunningBorrowedBalance(runningBorrowedBalance);

            // Filter by date range for output if specified, while retaining accurate historical running balance
            boolean afterStart = startDate == null || !tx.getTransactionDate().isBefore(startDate);
            boolean beforeEnd = endDate == null || !tx.getTransactionDate().isAfter(endDate);

            if (afterStart && beforeEnd) {
                entries.add(entry);
            }
        }

        ledger.setTotalCredit(totalCredit);
        ledger.setTotalPaid(totalPaid);
        ledger.setCurrentOutstandingBalance(runningBalance);
        ledger.setTotalBorrowed(totalBorrowed);
        ledger.setTotalRepaid(totalRepaid);
        ledger.setCurrentBorrowedBalance(runningBorrowedBalance);
        ledger.setEntries(entries);

        return ledger;
    }

    public String generateLedgerCsv(Long userId, Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        LedgerResponse ledger = getCustomerLedger(userId, customerId, startDate, endDate);
        StringBuilder sb = new StringBuilder();

        sb.append("Credit Book Ledger Report\n");
        sb.append("Customer: ").append(ledger.getCustomerName()).append("\n");
        sb.append("Mobile: ").append(ledger.getCustomerMobile()).append("\n");
        sb.append("Opening Balance: ₹").append(ledger.getOpeningBalance()).append("\n");
        sb.append("Total Credit Given: ₹").append(ledger.getTotalCredit()).append("\n");
        sb.append("Total Payments Received: ₹").append(ledger.getTotalPaid()).append("\n");
        sb.append("Current Outstanding Balance: ₹").append(ledger.getCurrentOutstandingBalance()).append("\n\n");

        sb.append("Date,Type,Description,Payment Method,Credit (+),Payment (-),Running Balance,Status\n");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (LedgerEntryDto entry : ledger.getEntries()) {
            sb.append(entry.getTransactionDate().format(dtf)).append(",");
            sb.append(entry.getTransactionType()).append(",");
            sb.append("\"").append(entry.getDescription() != null ? entry.getDescription().replace("\"", "\"\"") : "").append("\",");
            sb.append(entry.getPaymentMethod()).append(",");
            sb.append(entry.getCreditAmount().compareTo(BigDecimal.ZERO) > 0 ? entry.getCreditAmount() : "").append(",");
            sb.append(entry.getPaymentAmount().compareTo(BigDecimal.ZERO) > 0 ? entry.getPaymentAmount() : "").append(",");
            sb.append(entry.getRunningBalance()).append(",");
            sb.append(entry.isReversed() ? "REVERSED" : "ACTIVE").append("\n");
        }

        return sb.toString();
    }
}
