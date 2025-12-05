# Core Banking System (Microservices)

This project implements a modular banking system refactored into **8 granular microservices**, demonstrating a hybrid architecture that combines **Service-Oriented Architecture (SOA)** for core banking and **Event-Driven Architecture (EDA)** for payments.

---

## 🏗️ Architecture Overview

The system is divided into two logical modules:

### Module M1: Core Accounts (SOA)
**Role:** The "Source of Truth". Handles atomic account management, ledger consistency, and user identity.
**Communication:** Synchronous REST APIs.
**Database:** PostgreSQL (Shared Schema).

| Microservice | Port | Function |
| :--- | :--- | :--- |
| **Auth Service** | `8081` | **Identity Provider**. Handles user registration, login, and JWT/Session management. |
| **Technical Service** | `8082` | **Atomic Operations**. Manages low-level account states (e.g., `lockFunds`, `verifyAccount`). |
| **Business Function Service** | `8083` | **Read-Only Aggregator**. Optimized for queries like `getBalance`, `getTransactionHistory`. |
| **Business Transaction Service** | `8084` | **Write Operations**. Handles complex transactional logic like `transfer` and `deposit`. |

### Module M2: Payments (EDA)
**Role:** High-volume, asynchronous payment processing pipeline.
**Communication:** Asynchronous Kafka Events.
**Database:** Cassandra (Transaction History), Redis (Caching - Optional).

| Microservice | Port | Function |
| :--- | :--- | :--- |
| **Payment Gateway** | `8085` | **Ingestion Point**. Validates requests and publishes initial events to Kafka. |
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

### 3. Partner Deposit (Operations Clerk)
*   **Flow**: Hybrid (REST -> Kafka -> REST)
*   **Steps**:
    1.  **Initiation**: Clerk calls `Payment Gateway` (`POST /api/payments/deposit`).
    2.  **Event 1**: Gateway publishes `PaymentEvent` to **Kafka Topic:** `transaction_processing`.
        *   *Type:* `DEPOSIT`
    3.  **Fraud Check**: `Fraud Service` validates and publishes to `payment_clearing`.
    4.  **Settlement**: `Clearing Service` calls M1 `Business Transaction Service`.
        *   *Endpoint:* `POST http://localhost:8084/api/business/transactions/deposit`
    5.  **Completion**: Notification sent via Kafka.

---

## 👥 Default Users

| Role | Username | Password | User ID | Permissions |
| :--- | :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | `1` | Create accounts. |
| **Customer** | `alice` | `alice123` | `2` | Transfer money. |
| **Customer** | `bob` | `bob123` | `3` | Receive money. |
| **Clerk** | `clerk` | `clerk123` | `4` | Deposit funds. |

---

## 🛠️ Setup & Running

### 1. Start Infrastructure
```powershell
docker-compose up -d
```
*Starts PostgreSQL, Cassandra, Zookeeper, and Kafka.*

### 2. Run Microservices
Open **8 separate terminals** and run `mvn spring-boot:run` in each directory:
1.  `auth-service`
2.  `technical-service`
3.  `business-function-service`
4.  `business-transaction-service`
5.  `payment-gateway`
6.  `fraud-service`
7.  `clearing-service`
8.  `notification-service`

### 3. Run CLI
```powershell
./banking_cli.ps1
```
