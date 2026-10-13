package com.creditbook.config;

import com.creditbook.entity.*;
import com.creditbook.repository.CustomerRepository;
import com.creditbook.repository.TransactionRepository;
import com.creditbook.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CustomerRepository customerRepository,
                           TransactionRepository transactionRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            // Seed Demo User
            User demoUser = new User(
                    "Rajesh Sharma",
                    "demo@creditbook.com",
                    passwordEncoder.encode("Password@123"),
                    "Sharma General Store",
                    "+91 9876543210"
            );
            userRepository.save(demoUser);

            // Seed Second User for user isolation verification
            User user2 = new User(
                    "Anita Desai",
                    "merchant2@creditbook.com",
                    passwordEncoder.encode("Password@123"),
                    "Desai Textiles",
                    "+91 9811122233"
            );
            userRepository.save(user2);

            // Seed Customers for Demo User
            Customer c1 = new Customer(demoUser, "Ramesh Patel", "+91 9822011223", "ramesh.patel@example.com",
                    "Shop 12, Market Yard, Pune", "Wholesale groceries customer; weekly settlement", BigDecimal.ZERO);
            Customer c2 = new Customer(demoUser, "Priya Sundaram", "+91 9733445566", "priya.sundaram@example.com",
                    "B-402 Green Acres, Kothrud", "Regular neighborhood shopper", BigDecimal.ZERO);
            Customer c3 = new Customer(demoUser, "Amit Verma", "+91 9988776655", "amit.verma@example.com",
                    "Flat 101, Sunshine Heights", "Frequent buyer; pays via UPI", BigDecimal.ZERO);
            Customer c4 = new Customer(demoUser, "Sunita Gupta", "+91 9123456789", "sunita.gupta@example.com",
                    "C-12 Shivaji Nagar, Pune", "Monthly ration customer", BigDecimal.ZERO);
            Customer c5 = new Customer(demoUser, "Vikram Malhotra", "+91 9345678901", "vikram.m@example.com",
                    "Sector 4, Aundh, Pune", "Hardware supplies contractor", BigDecimal.ZERO);
            Customer c6 = new Customer(demoUser, "Kavita Joshi", "+91 9456789012", "kavita.j@example.com",
                    "Lane 5, Koregaon Park", "Occasional dairy & snack orders", BigDecimal.ZERO);
            Customer c7 = new Customer(demoUser, "Deepak Rao", "+91 9567890123", "deepak.rao@example.com",
                    "FC Road, Deccan Gymkhana", "Settled all accounts recently", BigDecimal.ZERO);

            customerRepository.save(c1);
            customerRepository.save(c2);
            customerRepository.save(c3);
            customerRepository.save(c4);
            customerRepository.save(c5);
            customerRepository.save(c6);
            customerRepository.save(c7);

            // Seed Customer for User 2 (Isolation Test)
            Customer cIsolated = new Customer(user2, "Karan Singhania", "+91 9998887771", "karan@singhania.com",
                    "MG Road, Mumbai", "Isolated test customer", BigDecimal.ZERO);
            customerRepository.save(cIsolated);

            // Seed Transactions for Customer 1 (Ramesh: Credit 5000 + 2000, Paid 1500 + 500 => Outstanding 5000)
            transactionRepository.save(new Transaction(demoUser, c1, TransactionType.CREDIT_GIVEN, new BigDecimal("5000.00"),
                    LocalDateTime.now().minusDays(30), "Initial monthly ration items", PaymentMethod.CASH, null));
            transactionRepository.save(new Transaction(demoUser, c1, TransactionType.PAYMENT_RECEIVED, new BigDecimal("1500.00"),
                    LocalDateTime.now().minusDays(24), "Partial repayment via GPay", PaymentMethod.UPI, "UPI-99482103"));
            transactionRepository.save(new Transaction(demoUser, c1, TransactionType.CREDIT_GIVEN, new BigDecimal("2000.00"),
                    LocalDateTime.now().minusDays(18), "Additional spices & dry fruits", PaymentMethod.CASH, null));
            transactionRepository.save(new Transaction(demoUser, c1, TransactionType.PAYMENT_RECEIVED, new BigDecimal("500.00"),
                    LocalDateTime.now().minusDays(12), "Weekly installment paid in cash", PaymentMethod.CASH, null));

            // Customer 2 (Priya: Credit 3500, Paid 1000 => Outstanding 2500)
            transactionRepository.save(new Transaction(demoUser, c2, TransactionType.CREDIT_GIVEN, new BigDecimal("3500.00"),
                    LocalDateTime.now().minusDays(20), "Organic grains & kitchen staples", PaymentMethod.CASH, null));
            transactionRepository.save(new Transaction(demoUser, c2, TransactionType.PAYMENT_RECEIVED, new BigDecimal("1000.00"),
                    LocalDateTime.now().minusDays(8), "UPI transfer through PhonePe", PaymentMethod.UPI, "UPI-8841293"));

            // Customer 3 (Amit: Credit 8200, Paid 3200 => Outstanding 5000)
            transactionRepository.save(new Transaction(demoUser, c3, TransactionType.CREDIT_GIVEN, new BigDecimal("8200.00"),
                    LocalDateTime.now().minusDays(15), "Festival celebration groceries bulk", PaymentMethod.CASH, null));
            transactionRepository.save(new Transaction(demoUser, c3, TransactionType.PAYMENT_RECEIVED, new BigDecimal("3200.00"),
                    LocalDateTime.now().minusDays(5), "Bank NEFT transfer", PaymentMethod.BANK_TRANSFER, "NEFT-AXIS-7712"));

            // Customer 4 (Sunita: Credit 1800, Paid 0 => Outstanding 1800)
            transactionRepository.save(new Transaction(demoUser, c4, TransactionType.CREDIT_GIVEN, new BigDecimal("1800.00"),
                    LocalDateTime.now().minusDays(7), "Dairy, pulses and cooking oil", PaymentMethod.CASH, null));

            // Customer 5 (Vikram: Credit 12000, Paid 4000 => Outstanding 8000)
            transactionRepository.save(new Transaction(demoUser, c5, TransactionType.CREDIT_GIVEN, new BigDecimal("12000.00"),
                    LocalDateTime.now().minusDays(14), "Building maintenance supplies", PaymentMethod.CASH, null));
            transactionRepository.save(new Transaction(demoUser, c5, TransactionType.PAYMENT_RECEIVED, new BigDecimal("4000.00"),
                    LocalDateTime.now().minusDays(3), "Cheque clearing", PaymentMethod.CHEQUE, "CHQ-44912"));

            // Customer 6 (Kavita: Credit 2400, Paid 1200 => Outstanding 1200)
            transactionRepository.save(new Transaction(demoUser, c6, TransactionType.CREDIT_GIVEN, new BigDecimal("2400.00"),
                    LocalDateTime.now().minusDays(10), "Weekend party snacks and beverages", PaymentMethod.CASH, null));
            transactionRepository.save(new Transaction(demoUser, c6, TransactionType.PAYMENT_RECEIVED, new BigDecimal("1200.00"),
                    LocalDateTime.now().minusDays(2), "Cash received at shop counter", PaymentMethod.CASH, null));

            // Customer 7 (Deepak: Credit 2500, Paid 2500 => Outstanding 0.00 - Settled)
            transactionRepository.save(new Transaction(demoUser, c7, TransactionType.CREDIT_GIVEN, new BigDecimal("2500.00"),
                    LocalDateTime.now().minusDays(9), "Household pantry items", PaymentMethod.CASH, null));
            transactionRepository.save(new Transaction(demoUser, c7, TransactionType.PAYMENT_RECEIVED, new BigDecimal("2500.00"),
                    LocalDateTime.now().minusDays(1), "Full settlement paid via Paytm UPI", PaymentMethod.UPI, "UPI-PTM-33291"));

            // User 2 Transaction (Isolated)
            transactionRepository.save(new Transaction(user2, cIsolated, TransactionType.CREDIT_GIVEN, new BigDecimal("15000.00"),
                    LocalDateTime.now().minusDays(6), "Textile bulk fabric order", PaymentMethod.BANK_TRANSFER, "IMPS-2026-991"));
        }
    }
}
