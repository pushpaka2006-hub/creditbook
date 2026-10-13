package com.creditbook.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LedgerResponse {

    private Long customerId;
    private String customerName;
    private String customerMobile;
    private String customerEmail;
    private String customerAddress;
    private BigDecimal openingBalance = BigDecimal.ZERO;
    private BigDecimal totalCredit = BigDecimal.ZERO;
    private BigDecimal totalPaid = BigDecimal.ZERO;
    private BigDecimal currentOutstandingBalance = BigDecimal.ZERO;
    private BigDecimal totalBorrowed = BigDecimal.ZERO;
    private BigDecimal totalRepaid = BigDecimal.ZERO;
    private BigDecimal currentBorrowedBalance = BigDecimal.ZERO;
    private List<LedgerEntryDto> entries = new ArrayList<>();

    public LedgerResponse() {}

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerMobile() {
        return customerMobile;
    }

    public void setCustomerMobile(String customerMobile) {
        this.customerMobile = customerMobile;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }

    public BigDecimal getTotalCredit() {
        return totalCredit;
    }

    public void setTotalCredit(BigDecimal totalCredit) {
        this.totalCredit = totalCredit;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public BigDecimal getCurrentOutstandingBalance() {
        return currentOutstandingBalance;
    }

    public void setCurrentOutstandingBalance(BigDecimal currentOutstandingBalance) {
        this.currentOutstandingBalance = currentOutstandingBalance;
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

    public BigDecimal getCurrentBorrowedBalance() {
        return currentBorrowedBalance;
    }

    public void setCurrentBorrowedBalance(BigDecimal currentBorrowedBalance) {
        this.currentBorrowedBalance = currentBorrowedBalance;
    }

    public List<LedgerEntryDto> getEntries() {
        return entries;
    }

    public void setEntries(List<LedgerEntryDto> entries) {
        this.entries = entries;
    }
}
