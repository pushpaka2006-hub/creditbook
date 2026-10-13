package com.creditbook.service;

import com.creditbook.dto.CustomerResponse;
import com.creditbook.dto.DashboardSummaryDto;
import com.creditbook.dto.MonthlyChartDto;
import com.creditbook.dto.TransactionResponse;
import com.creditbook.entity.Customer;
import com.creditbook.entity.Transaction;
import com.creditbook.entity.TransactionType;
import com.creditbook.repository.CustomerRepository;
import com.creditbook.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerService customerService;
    private final TransactionService transactionService;

    public DashboardService(CustomerRepository customerRepository,
                            TransactionRepository transactionRepository,
                            CustomerService customerService,
                            TransactionService transactionService) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.customerService = customerService;
        this.transactionService = transactionService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummary(Long userId) {
        DashboardSummaryDto summary = new DashboardSummaryDto();

        // 1. Total Customers
        long customerCount = customerRepository.countByUserId(userId);
        summary.setTotalCustomers(customerCount);

        // 2. Aggregate sums from database
        BigDecimal totalLent = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.CREDIT_GIVEN);
        BigDecimal totalRepaid = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.PAYMENT_RECEIVED);
        BigDecimal totalBorrowed = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.CREDIT_BORROWED);
        BigDecimal totalPaidBack = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.PAYMENT_MADE);

        totalLent = totalLent != null ? totalLent : BigDecimal.ZERO;
        totalRepaid = totalRepaid != null ? totalRepaid : BigDecimal.ZERO;
        totalBorrowed = totalBorrowed != null ? totalBorrowed : BigDecimal.ZERO;
        totalPaidBack = totalPaidBack != null ? totalPaidBack : BigDecimal.ZERO;

        summary.setTotalMoneyLent(totalLent);
        summary.setTotalPaymentsReceived(totalRepaid);
        summary.setTotalMoneyBorrowed(totalBorrowed);
        summary.setTotalMoneyRepaid(totalPaidBack);

        // Outstanding customer debt = sum of each customer's outstanding balance
        List<Customer> customers = customerRepository.findByUserIdOrderByFullNameAsc(userId);
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        long pendingCount = 0;
        List<CustomerResponse> customerSummaries = new ArrayList<>();

        for (Customer c : customers) {
            CustomerResponse res = customerService.mapToResponse(c, userId);
            customerSummaries.add(res);

            if (res.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0) {
                totalOutstanding = totalOutstanding.add(res.getOutstandingBalance());
                pendingCount++;
            }
        }

        summary.setTotalOutstandingCredit(totalOutstanding);
        summary.setTotalOutstandingPayable(totalBorrowed.subtract(totalPaidBack));
        summary.setPendingPaymentCount(pendingCount);

        // 3. Top Debtors (highest outstanding balance descending)
        List<CustomerResponse> topDebtors = customerSummaries.stream()
                .filter(c -> c.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> b.getOutstandingBalance().compareTo(a.getOutstandingBalance()))
                .limit(5)
                .collect(Collectors.toList());
        summary.setTopDebtors(topDebtors);

        // 4. Recent 8 transactions
        List<Transaction> recentTx = transactionRepository.findTop10ByUserIdOrderByTransactionDateDesc(userId);
        List<TransactionResponse> recentResponses = recentTx.stream()
                .map(transactionService::mapToResponse)
                .limit(8)
                .collect(Collectors.toList());
        summary.setRecentTransactions(recentResponses);

        // 5. Monthly Transaction Summary Chart (past 6 months)
        List<MonthlyChartDto> chartData = buildMonthlyChart(userId);
        summary.setMonthlyChart(chartData);

        return summary;
    }

    private List<MonthlyChartDto> buildMonthlyChart(Long userId) {
        List<MonthlyChartDto> list = new ArrayList<>();
        YearMonth current = YearMonth.now();
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

        // Last 6 months in chronological order
        for (int i = 5; i >= 0; i--) {
            YearMonth targetMonth = current.minusMonths(i);
            LocalDateTime start = targetMonth.atDay(1).atStartOfDay();
            LocalDateTime end = targetMonth.atEndOfMonth().atTime(23, 59, 59);

            BigDecimal credit = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                    userId, TransactionType.CREDIT_GIVEN, start, end);
            BigDecimal payment = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                    userId, TransactionType.PAYMENT_RECEIVED, start, end);

            credit = credit != null ? credit : BigDecimal.ZERO;
            payment = payment != null ? payment : BigDecimal.ZERO;

            MonthlyChartDto point = new MonthlyChartDto(targetMonth.format(labelFormatter), credit, payment);
            list.add(point);
        }

        return list;
    }
}
