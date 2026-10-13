package com.creditbook.dto;

import java.math.BigDecimal;

public class MonthlyChartDto {

    private String month;
    private BigDecimal creditGiven = BigDecimal.ZERO;
    private BigDecimal paymentReceived = BigDecimal.ZERO;
    private BigDecimal netDifference = BigDecimal.ZERO;

    public MonthlyChartDto() {}

    public MonthlyChartDto(String month, BigDecimal creditGiven, BigDecimal paymentReceived) {
        this.month = month;
        this.creditGiven = creditGiven != null ? creditGiven : BigDecimal.ZERO;
        this.paymentReceived = paymentReceived != null ? paymentReceived : BigDecimal.ZERO;
        this.netDifference = this.creditGiven.subtract(this.paymentReceived);
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getCreditGiven() {
        return creditGiven;
    }

    public void setCreditGiven(BigDecimal creditGiven) {
        this.creditGiven = creditGiven;
    }

    public BigDecimal getPaymentReceived() {
        return paymentReceived;
    }

    public void setPaymentReceived(BigDecimal paymentReceived) {
        this.paymentReceived = paymentReceived;
    }

    public BigDecimal getNetDifference() {
        return netDifference;
    }

    public void setNetDifference(BigDecimal netDifference) {
        this.netDifference = netDifference;
    }
}
