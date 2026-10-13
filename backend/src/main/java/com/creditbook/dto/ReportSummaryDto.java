package com.creditbook.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportSummaryDto {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal totalCreditGiven = BigDecimal.ZERO;
    private BigDecimal totalPaymentReceived = BigDecimal.ZERO;
    private BigDecimal netOutstanding = BigDecimal.ZERO;
    private BigDecimal totalBorrowed = BigDecimal.ZERO;
    private BigDecimal totalRepaid = BigDecimal.ZERO;
    private long totalTransactions;
    private List<MonthlyChartDto> monthlyBreakdown = new ArrayList<>();
    private List<CustomerResponse> customerReports = new ArrayList<>();

    public ReportSummaryDto() {}

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getTotalCreditGiven() {
        return totalCreditGiven;
    }

    public void setTotalCreditGiven(BigDecimal totalCreditGiven) {
        this.totalCreditGiven = totalCreditGiven;
    }

    public BigDecimal getTotalPaymentReceived() {
        return totalPaymentReceived;
    }

    public void setTotalPaymentReceived(BigDecimal totalPaymentReceived) {
        this.totalPaymentReceived = totalPaymentReceived;
    }

    public BigDecimal getNetOutstanding() {
        return netOutstanding;
    }

    public void setNetOutstanding(BigDecimal netOutstanding) {
        this.netOutstanding = netOutstanding;
    }

    public BigDecimal getTotalBorrowed() {
        return totalBorrowed;
    }

    public void setTotalBorrowed(BigDecimal totalBorrowed) {
        this.totalBorrowed = totalBorrowed;
    }

    public BigDecimal getTotalRepaid() {
        return totalRepaid;
    }

    public void setTotalRepaid(BigDecimal totalRepaid) {
        this.totalRepaid = totalRepaid;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public List<MonthlyChartDto> getMonthlyBreakdown() {
        return monthlyBreakdown;
    }

    public void setMonthlyBreakdown(List<MonthlyChartDto> monthlyBreakdown) {
        this.monthlyBreakdown = monthlyBreakdown;
    }

    public List<CustomerResponse> getCustomerReports() {
        return customerReports;
    }

    public void setCustomerReports(List<CustomerResponse> customerReports) {
        this.customerReports = customerReports;
    }
}
