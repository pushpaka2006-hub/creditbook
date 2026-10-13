package com.creditbook.dto;

import com.creditbook.entity.CustomerStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomerResponse {

    private Long id;
    private String fullName;
    private String mobile;
    private String email;
    private String address;
    private String notes;
    private CustomerStatus status;
    private BigDecimal openingBalance;
    private BigDecimal totalCreditGiven;
    private BigDecimal totalPaymentReceived;
    private BigDecimal outstandingBalance;
    private BigDecimal totalBorrowed;
    private BigDecimal totalRepaid;
    private BigDecimal borrowedBalance;
    private LocalDateTime lastTransactionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CustomerResponse() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public void setStatus(CustomerStatus status) {
        this.status = status;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
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

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
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

    public BigDecimal getBorrowedBalance() {
        return borrowedBalance;
    }

    public void setBorrowedBalance(BigDecimal borrowedBalance) {
        this.borrowedBalance = borrowedBalance;
    }

    public LocalDateTime getLastTransactionDate() {
        return lastTransactionDate;
    }

    public void setLastTransactionDate(LocalDateTime lastTransactionDate) {
        this.lastTransactionDate = lastTransactionDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
