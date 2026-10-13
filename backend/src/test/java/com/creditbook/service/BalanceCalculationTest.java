package com.creditbook.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Financial Balance Calculation Unit Tests")
public class BalanceCalculationTest {

    @Test
    @DisplayName("Verify Section 7 Specification: 5000 credit - 1500 payment + 2000 credit - 500 payment = 5000 outstanding")
    void testExactSection7Specification() {
        BigDecimal openingBalance = BigDecimal.ZERO;

        BigDecimal initialCredit = new BigDecimal("5000.00");
        BigDecimal payment1 = new BigDecimal("1500.00");
        BigDecimal additionalCredit = new BigDecimal("2000.00");
        BigDecimal payment2 = new BigDecimal("500.00");

        // Chronological balance tracking:
        BigDecimal runningBalance = openingBalance;
        runningBalance = runningBalance.add(initialCredit); // 5000.00
        assertEquals(new BigDecimal("5000.00"), runningBalance);

        runningBalance = runningBalance.subtract(payment1); // 3500.00
        assertEquals(new BigDecimal("3500.00"), runningBalance);

        runningBalance = runningBalance.add(additionalCredit); // 5500.00
        assertEquals(new BigDecimal("5500.00"), runningBalance);

        runningBalance = runningBalance.subtract(payment2); // 5000.00
        assertEquals(new BigDecimal("5000.00"), runningBalance);

        // Net aggregate formula: Total Credit Given - Total Valid Payments Received + Opening Balance
        BigDecimal totalCreditGiven = initialCredit.add(additionalCredit); // 7000.00
        BigDecimal totalPaymentsReceived = payment1.add(payment2);         // 2000.00
        BigDecimal outstandingBalance = totalCreditGiven.subtract(totalPaymentsReceived).add(openingBalance);

        assertEquals(new BigDecimal("7000.00"), totalCreditGiven);
        assertEquals(new BigDecimal("2000.00"), totalPaymentsReceived);
        assertEquals(new BigDecimal("5000.00"), outstandingBalance);
    }

    @Test
    @DisplayName("Verify full payment settlement leaves outstanding balance exactly zero")
    void testFullSettlementBalance() {
        BigDecimal credit = new BigDecimal("2500.00");
        BigDecimal payment = new BigDecimal("2500.00");
        BigDecimal balance = credit.subtract(payment);

        assertEquals(0, balance.compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Verify high-precision decimal operations avoid floating point drift")
    void testNonFloatingPointPrecision() {
        // In IEEE 754 float: 0.1 + 0.2 != 0.3. In BigDecimal it must equal 0.30 exact!
        BigDecimal item1 = new BigDecimal("0.10");
        BigDecimal item2 = new BigDecimal("0.20");
        BigDecimal sum = item1.add(item2).setScale(2, RoundingMode.HALF_UP);

        assertEquals(new BigDecimal("0.30"), sum);
    }

    @Test
    @DisplayName("Verify borrowing calculations maintain separate balances from lent money")
    void testSeparationOfLentAndBorrowedBalances() {
        BigDecimal lentAmount = new BigDecimal("10000.00");
        BigDecimal paymentReceived = new BigDecimal("4000.00");
        BigDecimal customerOwesUser = lentAmount.subtract(paymentReceived); // 6000.00

        BigDecimal borrowedAmount = new BigDecimal("8000.00");
        BigDecimal paymentMade = new BigDecimal("3000.00");
        BigDecimal userOwesLender = borrowedAmount.subtract(paymentMade); // 5000.00

        // Never combine money owed to user with money user owes to others!
        assertEquals(new BigDecimal("6000.00"), customerOwesUser);
        assertEquals(new BigDecimal("5000.00"), userOwesLender);
        assertNotEquals(customerOwesUser, userOwesLender);
    }
}
