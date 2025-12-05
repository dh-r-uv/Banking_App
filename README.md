# Core Banking System (Microservices)

This project implements a modular banking system refactored into **8 granular microservices**, demonstrating a hybrid architecture that combines **Microservices Architecture (SOA)** for core banking and lending and **Event-Driven Architecture (EDA)** for payments.

---

## 🏗️ Architecture Overview

The system is organized into two main modules:

### 📂 Module M1: Core Accounts (SOA)
**Location:** `./M1-CoreAccounts/`
**Role:** The "Source of Truth". Handles atomic account management, ledger consistency, and user identity.
**Communication:** Synchronous REST APIs.
**Database:** PostgreSQL (Shared Schema).

| Microservice | Port | Function |
| :--- | :--- | :--- |
| **Auth Service** | `8081` | **Identity Provider**. Handles user registration, login, and JWT/Session management. |
| **Technical Service** | `8082` | **Atomic Operations**. Manages low-level account states (e.g., `lockFunds`, `verifyAccount`). |
| **Business Function Service** | `8083` | **Read-Only Aggregator**. Optimized for queries like `getBalance`, `getTransactionHistory`. |
| **Business Transaction Service** | `8084` | **Write Operations**. Handles complex transactional logic like `transfer` and `deposit`. |

### 📂 Module M4: Lending & Credit (Microservices)
**Location:** `./M4-Lending/`
**Role:** Manages the entire loan lifecycle from application to repayment.
**Communication:** Synchronous REST (Internal & External).
**Database:** PostgreSQL (Loan & Repayment Tables).

| Microservice | Port | Function |
| :--- | :--- | :--- |
| **Credit Engine** | `8089` | **Technical Function**. Calculates credit scores (DTI rules) and validates collateral. |
| **Loan Servicing** | `8090` | **Business Process**. Orchestrates origination, disbursement, repayment, and reporting. |

---

## 🔄 Key Workflows & Event Chains

### 1. Authentication
*   **Flow**: Synchronous REST
*   **Steps**:
    1.  User sends credentials to `Auth Service` (`POST /api/auth/login`).
    2.  Service validates against PostgreSQL.
    3.  Returns `User ID` and Role (used in headers for subsequent requests).

### 2. P2P Money Transfer (Customer)
*   **Flow**: Hybrid (REST -> Kafka -> REST)
*   **Steps**:
    1.  **Initiation**: Customer calls `Payment Gateway` (`POST /api/payments`).
    2.  **Event 1**: Gateway publishes `PaymentEvent` to **Kafka Topic:** `transaction_processing`.
    3.  **Fraud Check**: `Fraud Service` consumes the event.
    4.  **Settlement**: `Clearing Service` calls M1 `Business Transaction Service` to update balances atomically.
    5.  **Completion**: `Notification Service` alerts the user.

### 3. Partner Deposit (Operations Clerk)
*   **Flow**: Hybrid (REST -> Kafka -> REST)
*   **Steps**:
    1.  **Initiation**: Clerk calls `Payment Gateway` (`POST /api/payments/deposit`).
    2.  **Event**: Gateway publishes `PaymentEvent` (Type: `DEPOSIT`).
    3.  **Settlement**: `Clearing Service` calls M1 to credit the account.

### 4. Loan Application & Disbursement (Customer)
*   **Flow**: Synchronous REST -> Hybrid Payment
*   **Steps**:
    1.  **Application**: Customer calls `Loan Servicing` (`POST /api/loan/apply`).
    2.  **Credit Check**: `Loan Servicing` calls `Credit Engine` (`GET /score`).
        *   *Logic*: Checks Income, DTI (Debt-to-Income), and Collateral.
        *   *Outcome*: Score > 700 = **APPROVED**.
    3.  **Validation**: `Loan Servicing` validates the Target Account with M1 `Business Function Service`.
    4.  **Disbursement**: If Approved & Valid, `Loan Servicing` calls `Payment Gateway` (`POST /deposit`) to fund the account.
    5.  **Repayment Scheduling**: Amortization schedule is generated automatically.

### 5. Loan Repayment
*   **Flow**: Synchronous REST
*   **Steps**:
    1.  Customer calls `Loan Servicing` (`POST /api/loan/repay`).
    2.  System applies payment to the oldest unpaid installment.
    3.  **Partial Payment Logic**: Excess amount reduces the *next* installment's balance.

---

## 👥 Default Users

| Role | Username | Password | User ID | Permissions |
| :--- | :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | `1` | Create accounts, View All Loans. |
| **Customer** | `alice` | `alice123` | `2` | Transfer, Apply for Loan, Repay. |
| **Customer** | `bob` | `bob123` | `3` | Receive money. |
| **Clerk** | `clerk` | `clerk123` | `4` | Deposit funds. |

---

## 🛠️ Setup & Running

### 1. Start Infrastructure
Run the following command in the root directory:
```powershell
docker-compose up -d
```
*Starts PostgreSQL, Zookeeper, and Kafka.*

### 2. Run M1: Core Accounts (Terminals 1-4)
1.  `cd M1-CoreAccounts/auth-service` -> `mvn spring-boot:run`
2.  `cd M1-CoreAccounts/technical-service` -> `mvn spring-boot:run`
3.  `cd M1-CoreAccounts/business-function-service` -> `mvn spring-boot:run`
4.  `cd M1-CoreAccounts/business-transaction-service` -> `mvn spring-boot:run`

### 3. Run M2: Payments (Terminals 5-8)
5.  `cd M2-Payments/payment-gateway` -> `mvn spring-boot:run`
6.  `cd M2-Payments/fraud-service` -> `mvn spring-boot:run`
7.  `cd M2-Payments/clearing-service` -> `mvn spring-boot:run`
8.  `cd M2-Payments/notification-service` -> `mvn spring-boot:run`

### 4. Run M4: Lending (Terminals 9-10)
9.  `cd M4-Lending/credit-engine` -> `mvn spring-boot:run`
10. `cd M4-Lending/loan-servicing` -> `mvn spring-boot:run`

### 5. Run CLI
In a new terminal (root directory):
```powershell
./banking_cli.ps1
```
