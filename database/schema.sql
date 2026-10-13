-- ==============================================================================
-- CREDIT BOOK DATABASE SCHEMA
-- Target Database: MySQL 8.0+
-- Application: Credit Book Full-Stack Management System
-- Character Set: utf8mb4, Collation: utf8mb4_unicode_ci
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS creditbook_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE creditbook_db;

-- ------------------------------------------------------------------------------
-- 1. Table: users
-- Stores registered business owners / shopkeepers with BCrypt hashed passwords.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    business_name VARCHAR(150),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 2. Table: customers
-- Customers or lending parties belonging to a specific authenticated user.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    mobile VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    address VARCHAR(255),
    notes TEXT,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' NOT NULL,
    opening_balance DECIMAL(15, 2) DEFAULT 0.00 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_customers_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_customers_user_id (user_id),
    INDEX idx_customers_mobile (mobile),
    INDEX idx_customers_user_name (user_id, full_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 3. Table: transactions
-- Financial transactions: credit given, payment received, credit borrowed, payment made.
-- Precision: DECIMAL(15,2) for exact non-floating point financial calculations.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    transaction_type ENUM('CREDIT_GIVEN', 'PAYMENT_RECEIVED', 'CREDIT_BORROWED', 'PAYMENT_MADE') NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_date DATETIME NOT NULL,
    description VARCHAR(255),
    payment_method ENUM('CASH', 'UPI', 'BANK_TRANSFER', 'CHEQUE', 'OTHER') DEFAULT 'CASH',
    reference_number VARCHAR(100),
    is_reversed BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE RESTRICT,
    CONSTRAINT chk_transaction_amount CHECK (amount > 0),
    INDEX idx_transactions_user_id (user_id),
    INDEX idx_transactions_customer_id (customer_id),
    INDEX idx_transactions_date (transaction_date),
    INDEX idx_transactions_user_date (user_id, transaction_date),
    INDEX idx_transactions_user_customer (user_id, customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 4. Table: audit_logs
-- Immutable audit log of all financial and critical user operations.
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_audit_logs_user_id (user_id),
    INDEX idx_audit_logs_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
