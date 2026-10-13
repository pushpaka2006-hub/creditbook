package com.creditbook.dto;

import com.creditbook.entity.CustomerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class CustomerRequest {

    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
    private String fullName;

    @NotBlank(message = "Mobile number is required")
    @Size(min = 7, max = 20, message = "Mobile number must be between 7 and 20 digits")
    private String mobile;

    private String email;
    private String address;
    private String notes;
    private CustomerStatus status = CustomerStatus.ACTIVE;
    private BigDecimal openingBalance = BigDecimal.ZERO;

    public CustomerRequest() {}

    public CustomerRequest(String fullName, String mobile, String email, String address, String notes, CustomerStatus status, BigDecimal openingBalance) {
        this.fullName = fullName;
        this.mobile = mobile;
        this.email = email;
        this.address = address;
        this.notes = notes;
        this.status = status != null ? status : CustomerStatus.ACTIVE;
        this.openingBalance = openingBalance != null ? openingBalance : BigDecimal.ZERO;
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
}
