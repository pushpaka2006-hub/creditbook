package com.creditbook.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PendingPaymentDto {

    private Long customerId;
    private String customerName;
    private String mobile;
    private String email;
    private BigDecimal outstandingBalance;
    private LocalDateTime lastTransactionDate;
    private String reminderMessage;

    public PendingPaymentDto() {}

    public PendingPaymentDto(Long customerId, String customerName, String mobile, String email, 
                             BigDecimal outstandingBalance, LocalDateTime lastTransactionDate) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.mobile = mobile;
        this.email = email;
        this.outstandingBalance = outstandingBalance;
        this.lastTransactionDate = lastTransactionDate;
        this.reminderMessage = String.format("Hello %s, your outstanding balance is ₹%s. Please contact us to arrange payment. Thank you.",
                customerName, outstandingBalance != null ? outstandingBalance.toPlainString() : "0.00");
    }

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

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
        this.reminderMessage = String.format("Hello %s, your outstanding balance is ₹%s. Please contact us to arrange payment. Thank you.",
                customerName, outstandingBalance != null ? outstandingBalance.toPlainString() : "0.00");
    }

    public LocalDateTime getLastTransactionDate() {
        return lastTransactionDate;
    }

    public void setLastTransactionDate(LocalDateTime lastTransactionDate) {
        this.lastTransactionDate = lastTransactionDate;
    }

    public String getReminderMessage() {
        return reminderMessage;
    }

    public void setReminderMessage(String reminderMessage) {
        this.reminderMessage = reminderMessage;
    }
}
