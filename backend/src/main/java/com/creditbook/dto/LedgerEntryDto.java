package com.creditbook.dto;

import com.creditbook.entity.PaymentMethod;
import com.creditbook.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LedgerEntryDto {

    private Long id;
    private LocalDateTime transactionDate;
    private TransactionType transactionType;
    private String description;
    private PaymentMethod paymentMethod;
    private String referenceNumber;
    private BigDecimal creditAmount = BigDecimal.ZERO;
    private BigDecimal paymentAmount = BigDecimal.ZERO;
    private BigDecimal borrowedAmount = BigDecimal.ZERO;
    private BigDecimal paidBackAmount = BigDecimal.ZERO;
    private BigDecimal runningBalance = BigDecimal.ZERO;
    private BigDecimal runningBorrowedBalance = BigDecimal.ZERO;
    private boolean reversed;

    public LedgerEntryDto() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public BigDecimal getBorrowedAmount() {
        return borrowedAmount;
    }

    public void setBorrowedAmount(BigDecimal borrowedAmount) {
        this.borrowedAmount = borrowedAmount;
    }

    public BigDecimal getPaidBackAmount() {
        return paidBackAmount;
    }

    public void setPaidBackAmount(BigDecimal paidBackAmount) {
        this.paidBackAmount = paidBackAmount;
    }

    public BigDecimal getRunningBalance() {
        return runningBalance;
    }

    public void setRunningBalance(BigDecimal runningBalance) {
        this.runningBalance = runningBalance;
    }

    public BigDecimal getRunningBorrowedBalance() {
        return runningBorrowedBalance;
    }

    public void setRunningBorrowedBalance(BigDecimal runningBorrowedBalance) {
        this.runningBorrowedBalance = runningBorrowedBalance;
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }
}
