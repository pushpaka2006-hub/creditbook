package com.creditbook.service;

import com.creditbook.dto.CustomerResponse;
import com.creditbook.dto.MonthlyChartDto;
import com.creditbook.dto.PendingPaymentDto;
import com.creditbook.dto.ReportSummaryDto;
import com.creditbook.entity.Customer;
import com.creditbook.entity.Transaction;
import com.creditbook.entity.TransactionType;
import com.creditbook.repository.CustomerRepository;
import com.creditbook.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerService customerService;

    public ReportService(CustomerRepository customerRepository,
                         TransactionRepository transactionRepository,
                         CustomerService customerService) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.customerService = customerService;
    }

    @Transactional(readOnly = true)
    public List<PendingPaymentDto> getPendingPayments(Long userId, String sortBy) {
        List<Customer> customers = customerRepository.findByUserIdOrderByFullNameAsc(userId);
        List<PendingPaymentDto> pendingList = new ArrayList<>();

        for (Customer c : customers) {
            CustomerResponse summary = customerService.mapToResponse(c, userId);
            if (summary.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0) {
                pendingList.add(new PendingPaymentDto(
                        c.getId(),
                        c.getFullName(),
                        c.getMobile(),
                        c.getEmail(),
                        summary.getOutstandingBalance(),
                        summary.getLastTransactionDate()
                ));
            }
        }

        if ("oldest".equalsIgnoreCase(sortBy)) {
            pendingList.sort(Comparator.comparing(
                    PendingPaymentDto::getLastTransactionDate,
                    Comparator.nullsLast(Comparator.naturalOrder())
            ));
        } else {
            // Default: highest balance first
            pendingList.sort((a, b) -> b.getOutstandingBalance().compareTo(a.getOutstandingBalance()));
        }

        return pendingList;
    }

    @Transactional(readOnly = true)
    public ReportSummaryDto getReportSummary(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        ReportSummaryDto report = new ReportSummaryDto();
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        LocalDateTime effectiveStart = startDate != null ? startDate : LocalDateTime.now().minusMonths(1);
        LocalDateTime effectiveEnd = endDate != null ? endDate : LocalDateTime.now();

        BigDecimal creditGiven = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.CREDIT_GIVEN, effectiveStart, effectiveEnd);
        BigDecimal paymentReceived = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.PAYMENT_RECEIVED, effectiveStart, effectiveEnd);
        BigDecimal borrowed = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.CREDIT_BORROWED, effectiveStart, effectiveEnd);
        BigDecimal repaid = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.PAYMENT_MADE, effectiveStart, effectiveEnd);

        creditGiven = creditGiven != null ? creditGiven : BigDecimal.ZERO;
        paymentReceived = paymentReceived != null ? paymentReceived : BigDecimal.ZERO;
        borrowed = borrowed != null ? borrowed : BigDecimal.ZERO;
        repaid = repaid != null ? repaid : BigDecimal.ZERO;

        report.setTotalCreditGiven(creditGiven);
        report.setTotalPaymentReceived(paymentReceived);
        report.setNetOutstanding(creditGiven.subtract(paymentReceived));
        report.setTotalBorrowed(borrowed);
        report.setTotalRepaid(repaid);

        List<Customer> customers = customerRepository.findByUserIdOrderByFullNameAsc(userId);
        List<CustomerResponse> customerReports = customers.stream()
                .map(c -> customerService.mapToResponse(c, userId))
                .collect(Collectors.toList());
        report.setCustomerReports(customerReports);

        return report;
    }

    public String generateTransactionsCsv(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> transactions = transactionRepository.filterTransactions(
                userId, null, null, startDate, endDate, null);

        StringBuilder sb = new StringBuilder();
        sb.append("Credit Book Transactions Report\n");
        sb.append("Generated At: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        sb.append("ID,Date,Customer,Mobile,Type,Amount,Payment Method,Description,Reference Number,Status\n");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Transaction tx : transactions) {
            sb.append(tx.getId()).append(",");
            sb.append(tx.getTransactionDate().format(dtf)).append(",");
            sb.append("\"").append(tx.getCustomer().getFullName().replace("\"", "\"\"")).append("\",");
            sb.append(tx.getCustomer().getMobile()).append(",");
            sb.append(tx.getTransactionType()).append(",");
            sb.append(tx.getAmount()).append(",");
            sb.append(tx.getPaymentMethod()).append(",");
            sb.append("\"").append(tx.getDescription() != null ? tx.getDescription().replace("\"", "\"\"") : "").append("\",");
            sb.append("\"").append(tx.getReferenceNumber() != null ? tx.getReferenceNumber().replace("\"", "\"\"") : "").append("\",");
            sb.append(tx.isReversed() ? "REVERSED" : "ACTIVE").append("\n");
        }

        return sb.toString();
    }

    public String generateCustomerReportCsv(Long userId) {
        List<Customer> customers = customerRepository.findByUserIdOrderByFullNameAsc(userId);
        StringBuilder sb = new StringBuilder();
        sb.append("Credit Book Customer Credit Summary\n");
        sb.append("Generated At: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        sb.append("Customer ID,Full Name,Mobile,Email,Status,Opening Balance,Total Credit,Total Paid,Net Outstanding Due,Total Borrowed,Total Repaid,Net Borrowed Due\n");

        for (Customer c : customers) {
            CustomerResponse res = customerService.mapToResponse(c, userId);
            sb.append(res.getId()).append(",");
            sb.append("\"").append(res.getFullName().replace("\"", "\"\"")).append("\",");
            sb.append(res.getMobile()).append(",");
            sb.append("\"").append(res.getEmail() != null ? res.getEmail().replace("\"", "\"\"") : "").append("\",");
            sb.append(res.getStatus()).append(",");
            sb.append(res.getOpeningBalance()).append(",");
            sb.append(res.getTotalCreditGiven()).append(",");
            sb.append(res.getTotalPaymentReceived()).append(",");
            sb.append(res.getOutstandingBalance()).append(",");
            sb.append(res.getTotalBorrowed()).append(",");
            sb.append(res.getTotalRepaid()).append(",");
            sb.append(res.getBorrowedBalance()).append("\n");
        }

        return sb.toString();
    }
}
