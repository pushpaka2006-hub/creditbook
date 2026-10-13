# Credit Book — Full-Stack Digital Financial Ledger Management System

> **Enterprise Full-Stack College & Academic Project Submission**  
> **Backend:** Java 21 • Spring Boot 3.3.4 / 3.5.4 • Spring Security (JWT + BCrypt) • Spring Data JPA • Hibernate  
> **Database:** MySQL 8.0+ (Normalized Tables • Foreign Keys • Indexes • ACID Transactions • `BigDecimal` Precision)  
> **Frontend:** React 18 • Vite • Tailwind CSS • Lucide Icons • Context API • SVG Analytics Engine  

---

## 1. Executive Summary

**Credit Book** is a modern, responsive web application designed to replace traditional paper-based bahi khata / credit ledger notebooks for shopkeepers, small business owners, and individuals. 

### Key Capabilities
1. **Accurate Financial Calculations:** Zero floating-point arithmetic. Every calculation uses `BigDecimal` in Java and `DECIMAL(15,2)` in MySQL to ensure precision down to the exact paisa/cent.
2. **Customer Ledger with Running Balance:** Dedicated ledger page per customer that chronologically calculates the running balance after every credit given, payment received, credit borrowed, or payment made.
3. **Transaction Reversals & Immutable Audit Log:** Errors can be safely reversed rather than silently deleted, maintaining full compliance, historical transparency, and an immutable audit trail (`audit_logs`).
4. **Smart Overpayment & Advance Prevention:** Strict validation prevents invalid payments that exceed a customer's outstanding balance unless specifically designated as an advance.
5. **Pending Dues & Payment Reminder Generator:** Instant tracking of customers with outstanding balances, sorted by highest due or oldest transaction, with one-click copyable payment reminders and direct WhatsApp Web messaging (`wa.me`).
6. **Multi-User Isolation:** Strict tenancy security ensures that one registered merchant can never view, mutate, or calculate balances belonging to another merchant.
7. **CSV & Print-Ready Exporting:** Instant client and server export of customer ledgers, transaction records, and debtor reports to CSV, along with print-optimized CSS for paper or PDF generation.

---

## 2. Technology Stack

| Layer | Technologies Used | Key Responsibilities |
|---|---|---|
| **Frontend** | React 18, Vite 5, Tailwind CSS, Lucide Icons, Axios | Responsive UI, interactive financial charts, search, filters, modals, print layout |
| **Backend** | Java 21 LTS, Spring Boot 3, Spring Data JPA, Hibernate, JJWT 0.12 | REST APIs, business validation, balance calculations, BCrypt auth, audit logging |
| **Database** | MySQL 8.0+ (with H2 in-memory test fallback) | Normalized storage, foreign key constraints, composite indexes, decimal precision |
| **Build Tools** | Maven 3.9 (`mvnw` included), npm 11 | Dependency resolution, packaging, test automation |
| **OS Support** | macOS (Apple Silicon & Intel), Linux, Windows | Cross-platform compatible |

---

## 3. Architecture & Data Flow

```
   ┌────────────────────────────────────────────────────────┐
   │             React 18 Single-Page Application           │
   │  (Dashboard, Ledger, Customers, Transactions, Reports) │
   └───────────────────────────┬────────────────────────────┘
                               │ HTTP / JSON (JWT Bearer Token)
                               ▼
   ┌────────────────────────────────────────────────────────┐
   │             Spring Boot REST API (Port 8080)           │
   │                                                        │
   │  [ Security Filter ] -> [ Controller ] -> [ Service ]  │
   │   (BCrypt / JJWT)       (DTO Validation)  (BigDecimal) │
   └───────────────────────────┬────────────────────────────┘
                               │ JPA / Hibernate
                               ▼
   ┌────────────────────────────────────────────────────────┐
   │            MySQL 8.0 Database (creditbook_db)          │
   │   (users, customers, transactions, audit_logs)         │
   └────────────────────────────────────────────────────────┘
```

---

## 4. Database Design (`database/schema.sql`)

The database consists of four normalized tables with referential integrity:

1. **`users`**:
   - `id` (BIGINT AUTO_INCREMENT PK)
   - `full_name` (VARCHAR(120))
   - `email` (VARCHAR(150) UNIQUE)
   - `password_hash` (VARCHAR(255) — BCrypt hash)
   - `business_name`, `phone`, `created_at`, `updated_at`

2. **`customers`**:
   - `id` (BIGINT AUTO_INCREMENT PK)
   - `user_id` (FK -> `users.id` ON DELETE CASCADE)
   - `full_name` (VARCHAR(150))
   - `mobile` (VARCHAR(20))
   - `email`, `address`, `notes`
   - `status` (`ACTIVE`, `INACTIVE`)
   - `opening_balance` (DECIMAL(15,2))
   - `created_at`, `updated_at`
   - Composite Index: `(user_id, full_name)`

3. **`transactions`**:
   - `id` (BIGINT AUTO_INCREMENT PK)
   - `user_id` (FK -> `users.id` ON DELETE CASCADE)
   - `customer_id` (FK -> `customers.id` ON DELETE RESTRICT)
   - `transaction_type` (`CREDIT_GIVEN`, `PAYMENT_RECEIVED`, `CREDIT_BORROWED`, `PAYMENT_MADE`)
   - `amount` (DECIMAL(15,2) CHECK amount > 0)
   - `transaction_date` (DATETIME)
   - `description`, `payment_method` (`CASH`, `UPI`, `BANK_TRANSFER`, `CHEQUE`, `OTHER`)
   - `reference_number` (VARCHAR(100))
   - `is_reversed` (BOOLEAN DEFAULT FALSE)
   - Composite Indexes: `(user_id, transaction_date)`, `(user_id, customer_id)`

4. **`audit_logs`**:
   - `id` (BIGINT AUTO_INCREMENT PK)
   - `user_id` (FK -> `users.id` ON DELETE CASCADE)
   - `action`, `entity_type`, `entity_id`, `details`, `ip_address`, `created_at`

---

## 5. macOS Setup & Execution in VS Code

### Prerequisites
Check your installed toolchains in macOS Terminal:
```bash
# 1. Verify Java 21
export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"
$JAVA_HOME/bin/java -version

# 2. Verify Node.js and npm
node -v    # v18+ or v24+
npm -v     # v9+ or v11+

# 3. Verify MySQL 8
mysql --version
```

---

### Step 1: Open the Project in VS Code
Open the project folder in Visual Studio Code:
```bash
cd /Users/manikanta/.gemini/antigravity/scratch/credit-book
code .
```

---

### Step 2: Database Setup (MySQL)
Start your local MySQL service and create the database:
```bash
# Start MySQL (if using Homebrew)
brew services start mysql

# Or if using standard macOS MySQL installation:
sudo /usr/local/mysql/support-files/mysql.server start

# Execute the database schema and sample data:
mysql -u root -p < database/schema.sql
mysql -u root -p < database/sample_data.sql
```

*(If your MySQL root user does not have a password, omit the `-p` flag).*

---

### Step 3: Configure Environment Variables
Copy `.env.example` to configure custom credentials (optional if using defaults):
```bash
# If your MySQL password is not empty:
export SPRING_DATASOURCE_PASSWORD="your_password_here"
```

---

### Step 4: Run the Backend (Terminal 1 in VS Code)
Open a new integrated terminal in VS Code:
```bash
# Terminal 1:
./run-backend.sh
```
Or run directly with the portable Maven runner:
```bash
cd backend
export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"
./mvnw spring-boot:run
```
Backend will start at: **`http://localhost:8080`**

---

### Step 5: Run the Frontend (Terminal 2 in VS Code)
Open a second integrated terminal in VS Code:
```bash
# Terminal 2:
./run-frontend.sh
```
Or run with npm:
```bash
cd frontend
npm run dev
```
Frontend will start at: **`http://localhost:5173`**

---

## 6. Default Demo Accounts

The database comes pre-seeded with sample merchant accounts and realistic customer transactions:

| Role / Scenario | Email | Password | Business Name |
|---|---|---|---|
| **Primary Demo Merchant** | `demo@creditbook.com` | `Password@123` | Sharma General Store |
| **Secondary Merchant (Isolation Test)** | `merchant2@creditbook.com` | `Password@123` | Desai Textiles |

*Tip: On the login page, you can click **"Fill Demo Credentials"** to log in instantly with 1 click.*

---

## 7. Verified Financial Test Scenarios

The test suite runs 16 automated tests covering financial math, CRUD, and API integration:

```bash
cd backend
export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"
./mvnw test -o
```

**Results:**
```
[INFO] Running com.creditbook.controller.AuthControllerTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.creditbook.service.CustomerServiceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.creditbook.service.BalanceCalculationTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.creditbook.service.TransactionServiceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Key Verification Cases
1. **Section 7 Core Specification:**
   - Initial Credit: ₹5,000.00
   - Payment Received: ₹1,500.00 (Balance = ₹3,500.00)
   - Additional Credit: ₹2,000.00 (Balance = ₹5,500.00)
   - Additional Payment: ₹500.00 (Remaining Outstanding Balance = ₹5,000.00)
   - Verified exact match in unit tests with `BigDecimal`.
2. **Deletion Protection:** Attempting to delete a customer who has non-reversed transactions throws an HTTP 400 with a detailed error message protecting history.
3. **Overpayment Prevention:** Entering a payment greater than the customer's outstanding balance throws an error unless designated as an advance.
4. **Reversal Audit Trail:** Reversing a transaction flags `reversed = true`, notes the reversal reason, and creates an audit record in `audit_logs`.
5. **Tenancy Isolation:** User A can never query, edit, or reverse User B's customers or transactions.

---

## 8. REST API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register new merchant |
| `POST` | `/api/auth/login` | Authenticate and obtain JWT |
| `POST` | `/api/auth/logout` | End session |
| `GET` | `/api/auth/me` | Fetch authenticated merchant profile |
| `GET` | `/api/customers` | List customers (supports `?search=` and `?status=`) |
| `POST` | `/api/customers` | Create new customer account |
| `GET` | `/api/customers/{id}` | Get customer summary and calculated balances |
| `PUT` | `/api/customers/{id}` | Update customer details |
| `DELETE` | `/api/customers/{id}` | Safe deletion (fails if active transactions exist) |
| `GET` | `/api/customers/{id}/ledger` | Chronological customer ledger with running balances |
| `GET` | `/api/customers/{id}/ledger/export/csv` | Download customer ledger as CSV |
| `GET` | `/api/transactions` | List all transactions with filters |
| `POST` | `/api/transactions` | Record credit, repayment, borrowing, or repayment |
| `GET` | `/api/transactions/{id}` | Get transaction details |
| `PUT` | `/api/transactions/{id}` | Update transaction description/method/ref |
| `POST` | `/api/transactions/{id}/reverse` | Reverse transaction safely |
| `GET` | `/api/dashboard/summary` | Real-time statistics, top debtors, and 6-month chart data |
| `GET` | `/api/reports/summary` | Summary reports by date range (today, week, month, custom) |
| `GET` | `/api/reports/pending` | List pending debtor accounts sorted by due or date |
| `GET` | `/api/reports/export/transactions/csv` | Download full transactions report as CSV |
| `GET` | `/api/reports/export/customers/csv` | Download customer balance summary report as CSV |
| `GET` | `/api/audit-logs` | Retrieve immutable audit trail |
| `PUT` | `/api/user/profile` | Update merchant store details |
| `POST` | `/api/user/password` | Change account password |

---

## 9. Troubleshooting & FAQ

- **Port 8080 already in use:**
  Run `lsof -i :8080` and terminate the process, or set `PORT=8081` in `.env`.
- **Port 5173 already in use:**
  Vite will automatically pick port 5174 if 5173 is occupied.
- **MySQL Connection Refused:**
  Ensure MySQL is running with `brew services start mysql` or `mysql.server start`. You can test with `mysqladmin -u root -p ping`.
- **How to stop servers:**
  Press `Ctrl + C` in Terminal 1 (backend) and Terminal 2 (frontend).

---

## 10. License & Submission Information
- **Course Submission:** Full-Stack Web Application Project
- **Project Name:** Credit Book
- **Status:** Complete & Production Ready
