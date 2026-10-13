-- ==============================================================================
-- CREDIT BOOK SAMPLE SEED DATA
-- Target Database: MySQL 8.0+
-- Default Demo User Credentials:
--   Email: demo@creditbook.com
--   Password: Password@123
--   (BCrypt: $2a$10$ySkgW1BwT2kE7xO0x7W6eeY9vjD1vC5LzK1C4lC0aD4p7kL2g2C1y)
-- Secondary Test User (for User-Isolation Verification):
--   Email: merchant2@creditbook.com
--   Password: Password@123
-- ==============================================================================

USE creditbook_db;

-- 1. Insert Demo Users
INSERT INTO users (id, full_name, email, password_hash, business_name, phone, created_at)
VALUES 
(1, 'Rajesh Sharma', 'demo@creditbook.com', '$2a$10$ySkgW1BwT2kE7xO0x7W6eeY9vjD1vC5LzK1C4lC0aD4p7kL2g2C1y', 'Sharma General Store', '+91 9876543210', NOW() - INTERVAL 60 DAY),
(2, 'Anita Desai', 'merchant2@creditbook.com', '$2a$10$ySkgW1BwT2kE7xO0x7W6eeY9vjD1vC5LzK1C4lC0aD4p7kL2g2C1y', 'Desai Textiles', '+91 9811122233', NOW() - INTERVAL 30 DAY)
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- 2. Insert Customers for Demo User (id = 1)
INSERT INTO customers (id, user_id, full_name, mobile, email, address, notes, status, opening_balance, created_at)
VALUES
(1, 1, 'Ramesh Patel', '+91 9822011223', 'ramesh.patel@example.com', 'Shop 12, Market Yard, Pune', 'Wholesale groceries customer; weekly settlement', 'ACTIVE', 0.00, NOW() - INTERVAL 45 DAY),
(2, 1, 'Priya Sundaram', '+91 9733445566', 'priya.sundaram@example.com', 'B-402 Green Acres, Kothrud', 'Regular neighborhood shopper', 'ACTIVE', 0.00, NOW() - INTERVAL 40 DAY),
(3, 1, 'Amit Verma', '+91 9988776655', 'amit.verma@example.com', 'Flat 101, Sunshine Heights', 'Frequent buyer; pays via UPI', 'ACTIVE', 0.00, NOW() - INTERVAL 35 DAY),
(4, 1, 'Sunita Gupta', '+91 9123456789', 'sunita.gupta@example.com', 'C-12 Shivaji Nagar, Pune', 'Monthly ration customer', 'ACTIVE', 0.00, NOW() - INTERVAL 25 DAY),
(5, 1, 'Vikram Malhotra', '+91 9345678901', 'vikram.m@example.com', 'Sector 4, Aundh, Pune', 'Hardware supplies contractor', 'ACTIVE', 0.00, NOW() - INTERVAL 20 DAY),
(6, 1, 'Kavita Joshi', '+91 9456789012', 'kavita.j@example.com', 'Lane 5, Koregaon Park', 'Occasional dairy & snack orders', 'ACTIVE', 0.00, NOW() - INTERVAL 15 DAY),
(7, 1, 'Deepak Rao', '+91 9567890123', 'deepak.rao@example.com', 'FC Road, Deccan Gymkhana', 'Settled all accounts recently', 'ACTIVE', 0.00, NOW() - INTERVAL 10 DAY)
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- 3. Insert Customers for Secondary User (id = 2) to test user isolation
INSERT INTO customers (id, user_id, full_name, mobile, email, address, notes, status, opening_balance, created_at)
VALUES
(8, 2, 'Karan Singhania', '+91 9998887771', 'karan@singhania.com', 'MG Road, Mumbai', 'Isolated test customer', 'ACTIVE', 0.00, NOW() - INTERVAL 10 DAY)
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- 4. Insert Transactions for Demo User (id = 1)
-- Customer 1: Ramesh Patel -> Credit 5,000 + 2,000; Repaid 1,500 + 500 => Balance = 5,000
INSERT INTO transactions (id, user_id, customer_id, transaction_type, amount, transaction_date, description, payment_method, is_reversed, created_at)
VALUES
(1, 1, 1, 'CREDIT_GIVEN', 5000.00, NOW() - INTERVAL 30 DAY, 'Initial monthly ration items', 'CASH', false, NOW() - INTERVAL 30 DAY),
(2, 1, 1, 'PAYMENT_RECEIVED', 1500.00, NOW() - INTERVAL 24 DAY, 'Partial repayment via GPay', 'UPI', false, NOW() - INTERVAL 24 DAY),
(3, 1, 1, 'CREDIT_GIVEN', 2000.00, NOW() - INTERVAL 18 DAY, 'Additional spices & dry fruits', 'CASH', false, NOW() - INTERVAL 18 DAY),
(4, 1, 1, 'PAYMENT_RECEIVED', 500.00, NOW() - INTERVAL 12 DAY, 'Weekly installment paid in cash', 'CASH', false, NOW() - INTERVAL 12 DAY);

-- Customer 2: Priya Sundaram -> Credit 3,500; Repaid 1,000 => Balance = 2,500
INSERT INTO transactions (id, user_id, customer_id, transaction_type, amount, transaction_date, description, payment_method, is_reversed, created_at)
VALUES
(5, 1, 2, 'CREDIT_GIVEN', 3500.00, NOW() - INTERVAL 20 DAY, 'Organic grains & kitchen staples', 'CASH', false, NOW() - INTERVAL 20 DAY),
(6, 1, 2, 'PAYMENT_RECEIVED', 1000.00, NOW() - INTERVAL 8 DAY, 'UPI transfer through PhonePe', 'UPI', false, NOW() - INTERVAL 8 DAY);

-- Customer 3: Amit Verma -> Credit 8,200; Repaid 3,200 => Balance = 5,000
INSERT INTO transactions (id, user_id, customer_id, transaction_type, amount, transaction_date, description, payment_method, is_reversed, created_at)
VALUES
(7, 1, 3, 'CREDIT_GIVEN', 8200.00, NOW() - INTERVAL 15 DAY, 'Festival celebration groceries bulk', 'CASH', false, NOW() - INTERVAL 15 DAY),
(8, 1, 3, 'PAYMENT_RECEIVED', 3200.00, NOW() - INTERVAL 5 DAY, 'Bank NEFT transfer', 'BANK_TRANSFER', false, NOW() - INTERVAL 5 DAY);

-- Customer 4: Sunita Gupta -> Credit 1,800; Repaid 0 => Balance = 1,800
INSERT INTO transactions (id, user_id, customer_id, transaction_type, amount, transaction_date, description, payment_method, is_reversed, created_at)
VALUES
(9, 1, 4, 'CREDIT_GIVEN', 1800.00, NOW() - INTERVAL 7 DAY, 'Dairy, pulses and cooking oil', 'CASH', false, NOW() - INTERVAL 7 DAY);

-- Customer 5: Vikram Malhotra -> Credit 12,000; Repaid 4,000 => Balance = 8,000
INSERT INTO transactions (id, user_id, customer_id, transaction_type, amount, transaction_date, description, payment_method, is_reversed, created_at)
VALUES
(10, 1, 5, 'CREDIT_GIVEN', 12000.00, NOW() - INTERVAL 14 DAY, 'Building maintenance supplies', 'CASH', false, NOW() - INTERVAL 14 DAY),
(11, 1, 5, 'PAYMENT_RECEIVED', 4000.00, NOW() - INTERVAL 3 DAY, 'Cheque clearing #CHQ-44912', 'CHEQUE', false, NOW() - INTERVAL 3 DAY);

-- Customer 6: Kavita Joshi -> Credit 2,400; Repaid 1,200 => Balance = 1,200
INSERT INTO transactions (id, user_id, customer_id, transaction_type, amount, transaction_date, description, payment_method, is_reversed, created_at)
VALUES
(12, 1, 6, 'CREDIT_GIVEN', 2400.00, NOW() - INTERVAL 10 DAY, 'Weekend party snacks and beverages', 'CASH', false, NOW() - INTERVAL 10 DAY),
(13, 1, 6, 'PAYMENT_RECEIVED', 1200.00, NOW() - INTERVAL 2 DAY, 'Cash received at shop counter', 'CASH', false, NOW() - INTERVAL 2 DAY);

-- Customer 7: Deepak Rao -> Credit 2,500; Repaid 2,500 => Balance = 0.00 (Fully Settled)
INSERT INTO transactions (id, user_id, customer_id, transaction_type, amount, transaction_date, description, payment_method, is_reversed, created_at)
VALUES
(14, 1, 7, 'CREDIT_GIVEN', 2500.00, NOW() - INTERVAL 9 DAY, 'Household pantry items', 'CASH', false, NOW() - INTERVAL 9 DAY),
(15, 1, 7, 'PAYMENT_RECEIVED', 2500.00, NOW() - INTERVAL 1 DAY, 'Full settlement paid via Paytm UPI', 'UPI', false, NOW() - INTERVAL 1 DAY);

-- Customer 8 (User 2): Isolated transaction
INSERT INTO transactions (id, user_id, customer_id, transaction_type, amount, transaction_date, description, payment_method, is_reversed, created_at)
VALUES
(16, 2, 8, 'CREDIT_GIVEN', 15000.00, NOW() - INTERVAL 6 DAY, 'Textile bulk fabric order', 'BANK_TRANSFER', false, NOW() - INTERVAL 6 DAY);

-- 5. Insert Audit Logs
INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details, created_at)
VALUES
(1, 'USER_REGISTER', 'USER', 1, 'User account registered successfully', NOW() - INTERVAL 60 DAY),
(1, 'CUSTOMER_CREATED', 'CUSTOMER', 1, 'Created customer: Ramesh Patel', NOW() - INTERVAL 45 DAY),
(1, 'TRANSACTION_RECORDED', 'TRANSACTION', 1, 'Credit given of ₹5000.00 to Ramesh Patel', NOW() - INTERVAL 30 DAY),
(1, 'TRANSACTION_RECORDED', 'TRANSACTION', 2, 'Payment received of ₹1500.00 from Ramesh Patel', NOW() - INTERVAL 24 DAY);
