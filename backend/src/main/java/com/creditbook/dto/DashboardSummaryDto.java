package com.creditbook.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DashboardSummaryDto {

    private long totalCustomers;
    private BigDecimal totalOutstandingCredit = BigDecimal.ZERO; // Money customers owe user
    private BigDecimal totalPaymentsReceived = BigDecimal.ZERO;
    private BigDecimal totalMoneyLent = BigDecimal.ZERO;
    private BigDecimal totalMoneyBorrowed = BigDecimal.ZERO;    // Money user borrowed
    private BigDecimal totalMoneyRepaid = BigDecimal.ZERO;
    private BigDecimal totalOutstandingPayable = BigDecimal.ZERO; // Money user owes others
    private long pendingPaymentCount;
    private List<TransactionResponse> recentTransactions = new ArrayList<>();
    private List<CustomerResponse> topDebtors = new ArrayList<>();
    private List<MonthlyChartDto> monthlyChart = new ArrayList<>();

    public DashboardSummaryDto() {}

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public BigDecimal getTotalOutstandingCredit() {
        return totalOutstandingCredit;
    }

    public void setTotalOutstandingCredit(BigDecimal totalOutstandingCredit) {
        this.totalOutstandingCredit = totalOutstandingCredit;
    }

    public BigDecimal getTotalPaymentsReceived() {
        return totalPaymentsReceived;
    }

    public void setTotalPaymentsReceived(BigDecimal totalPaymentsReceived) {
        this.totalPaymentsReceived = totalPaymentsReceived;
    }

    public BigDecimal getTotalMoneyLent() {
        return totalMoneyLent;
    }

    public void setTotalMoneyLent(BigDecimal totalMoneyLent) {
        this.totalMoneyLent = totalMoneyLent;
    }

    public BigDecimal getTotalMoneyBorrowed() {
        return totalMoneyBorrowed;
    }

    public void setTotalMoneyBorrowed(BigDecimal totalMoneyBorrowed) {
        this.totalMoneyBorrowed = totalMoneyBorrowed;
    }

    public BigDecimal getTotalMoneyRepaid() {
        return totalMoneyRepaid;
    }

    public void setTotalMoneyRepaid(BigDecimal totalMoneyRepaid) {
        this.totalMoneyRepaid = totalMoneyRepaid;
    }

    public BigDecimal getTotalOutstandingPayable() {
        return totalOutstandingPayable;
    }

    public void setTotalOutstandingPayable(BigDecimal totalOutstandingPayable) {
        this.totalOutstandingPayable = totalOutstandingPayable;
    }

    public long getPendingPaymentCount() {
        return pendingPaymentCount;
    }

    public void setPendingPaymentCount(long pendingPaymentCount) {
        this.pendingPaymentCount = pendingPaymentCount;
    }

    public List<TransactionResponse> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<TransactionResponse> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    public List<CustomerResponse> getTopDebtors() {
        return topDebtors;
    }

    public void setTopDebtors(List<CustomerResponse> topDebtors) {
        this.topDebtors = topDebtors;
    }

    public List<MonthlyChartDto> getMonthlyChart() {
        return monthlyChart;
    }

    public void setMonthlyChart(List<MonthlyChartDto> monthlyChart) {
        this.monthlyChart = monthlyChart;
    }
}
