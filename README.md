# Core Banking System (Microservices)

This project implements a modular banking system refactored into **8 granular microservices**, demonstrating a hybrid architecture that combines **Service-Oriented Architecture (SOA)** for core banking and **Event-Driven Architecture (EDA)** for payments.

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

### 📂 Module M2: Payments (EDA)
**Location:** `./M2-Payments/`
**Role:** High-volume, asynchronous payment processing pipeline.
**Communication:** Asynchronous Kafka Events.
**Database:** PostgreSQL (Payments Table).

| Microservice | Port | Function |
| :--- | :--- | :--- |
| **Payment Gateway** | `8085` | **Ingestion Point**. Validates requests, publishes events, and provides status feedback. |
| **Fraud Service** | `8086` | **Risk Engine**. Consumes events, applies fraud rules, and approves/rejects payments. |
| **Clearing Service** | `8087` | **Settlement Orchestrator**. Consumes cleared payments and calls M1 to update balances. |
| **Notification Service** | `8088` | **Alert System**. Consumes final status events and notifies users (simulated). |

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
        *   *Status:* `INITIATED`
    3.  **Fraud Check**: `Fraud Service` consumes the event.
        *   *Logic:* Checks if amount > Limit.
        *   *Outcome:* Publishes to **Kafka Topic:** `payment_clearing`.
        *   *Status:* `CLEARED` (or `FAILED`).
    4.  **Settlement (Cross-Module Communication)**: `Clearing Service` consumes the cleared event.
        *   **Action**: It makes a **Synchronous REST Call** to `Business Transaction Service` (M1).
        *   *Endpoint:* `POST http://localhost:8084/api/business/transactions/transfer`
        *   *Payload:* `{ fromAccount, toAccount, amount }`
        *   *Result:* M1 updates PostgreSQL balances atomically.
    5.  **Notification**: `Clearing Service` publishes to **Kafka Topic:** `notification_service`.
    6.  **Alert**: `Notification Service` logs the success message.
    7.  **Feedback**: CLI polls `Payment Gateway` (`GET /api/payments/{id}/status`) to confirm success.

### 3. Partner Deposit (Operations Clerk)
*   **Flow**: Hybrid (REST -> Kafka -> REST)
*   **Steps**:
    1.  **Initiation**: Clerk calls `Payment Gateway`.
        *   **Endpoint**: `POST /api/payments/deposit`
        *   **Payload**:
            ```json
            {
              "targetAccount": "1234567890",
              "amount": 1000.00
            }
            ```
    2.  **Event 1**: Gateway publishes `PaymentEvent` to **Kafka Topic:** `transaction_processing`.
        *   *Type:* `DEPOSIT`
        *   *Source:* `PARTNER_DEPOSIT`
    3.  **Fraud Check**: `Fraud Service` consumes the event.
        *   *Logic:* Checks if amount > Limit.
        *   *Outcome:* Publishes to **Kafka Topic:** `payment_clearing`.
    4.  **Settlement (Cross-Module Communication)**: `Clearing Service` consumes the cleared event.
        *   **Action**: Calls `Business Transaction Service` (M1).
        *   *Endpoint:* `POST http://localhost:8084/api/business/transactions/deposit`
        *   *Payload:* `{ accountNumber, amount }`
        *   *Result:* M1 credits the account in PostgreSQL.
    5.  **Completion**: `Clearing Service` publishes to **Kafka Topic:** `notification_service`.
    6.  **Alert**: `Notification Service` logs the success message.

---

## 👥 Default Users

| Role | Username | Password | User ID | Permissions |
| :--- | :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | `1` | Create accounts, Lock funds. |
| **Customer** | `alice` | `alice123` | `2` | Transfer money, View balance. |
| **Customer** | `bob` | `bob123` | `3` | Receive money, View balance. |
| **Clerk** | `clerk` | `clerk123` | `4` | Deposit funds. |

---

## 🛠️ Setup & Running

### 1. Start Infrastructure
Run the following command in the root directory:
```powershell
docker-compose up -d
```
*Starts PostgreSQL, Zookeeper, and Kafka.*

### 2. Run Microservices
You need to open **8 separate terminals**. Navigate to the respective folder and run the service.

**M1: Core Accounts**
1.  `cd M1-CoreAccounts/auth-service` -> `mvn spring-boot:run`
2.  `cd M1-CoreAccounts/technical-service` -> `mvn spring-boot:run`
3.  `cd M1-CoreAccounts/business-function-service` -> `mvn spring-boot:run`
4.  `cd M1-CoreAccounts/business-transaction-service` -> `mvn spring-boot:run`

**M2: Payments**
5.  `cd M2-Payments/payment-gateway` -> `mvn spring-boot:run`
6.  `cd M2-Payments/fraud-service` -> `mvn spring-boot:run`
7.  `cd M2-Payments/clearing-service` -> `mvn spring-boot:run`
8.  `cd M2-Payments/notification-service` -> `mvn spring-boot:run`

### 3. Run CLI
In a new terminal (root directory):
```powershell
./banking_cli.ps1
```
