package com.creditbook.entity;

public enum TransactionType {
    CREDIT_GIVEN,       // User lent money to customer (customer owes user)
    PAYMENT_RECEIVED,   // Customer repaid money to user (reduces customer's debt)
    CREDIT_BORROWED,    // User borrowed money from party (user owes party)
    PAYMENT_MADE        // User repaid money to party (reduces user's debt)
}
