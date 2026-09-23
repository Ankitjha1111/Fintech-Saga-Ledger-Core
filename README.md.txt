# Resilient Distributed Banking & Financial Ledger Engine

A production-grade, fault-tolerant Core Banking Backend System architected completely using Java Spring Boot Microservices. This platform manages distributed financial workflows utilizing the Orchestration-based Saga Pattern to enforce transactional consistency across service nodes, backed by a live Redis Distributed Cache for low-latency idempotency evaluations and strict MySQL Double-Entry Bookkeeping.

## Technical Core & System Architecture

* **Decoupled Microservices Infrastructure:** Fully decentralized service discovery powered by Netflix Eureka Server and managed through a unified Spring Cloud Gateway routing layer executing security filtering, rate-limiting, and centralized edge proxying.
* **Distributed Consistency (Orchestration Saga):** Implements an explicitly managed state-machine orchestration lifecycle (STARTED -> PAYMENT_COMPLETED -> LEDGER_UPDATED) controlling complex multi-service transactional sequences. Any down-stream step validation runtime failure triggers deterministic Compensating Transactions (Rollbacks).
* **High-Throughput Boundary Security (Idempotency):** Leverages an in-memory Redis Distributed Cache evaluating unique incoming client-side idempotencyKey headers in O(1) lookup time, entirely blocking financial double-deduction risks from duplicate client retries.
* **Compliance-Driven Double-Entry Bookkeeping:** Programmed a secure accounting engine logging atomic transaction updates directly within MySQL. For every exchange, concurrent debit and credit logs are permanently bound via a single UUID transaction_id ensuring a tamper-proof auditing trail.
* **Resilient Graceful Failure Safeguards:** Integrated automated validation check exceptions (e.g., Insufficient Balance), pairing advanced Spring transactional propagation constraints (Propagation.REQUIRES_NEW) to undo half-baked account balances while successfully securing a permanent FAILED state audit record.

## Technology Stack
* **Backend Framework:** Core Java, Spring Boot, Spring Cloud (Gateway, Discovery Cluster)
* **Distributed Caching:** Redis Cache Server (In-memory verification registry)
* **Persistent Database:** MySQL (Relational double-entry bookkeeping ledgers)
* **Testing & Debugging:** Postman HTTP Suite, Logback Logging Engine (SLF4J Log Contexts)

## End-to-End Execution Flow
1. **Payload Dispatch:** Client issues a financial transfer request (via Postman/API) attaching a cryptographically secure UUID idempotencyKey inside the REST header payload.
2. **Gateway Proxying:** Spring Cloud Gateway intercepts the ingress network traffic and maps the endpoints dynamically against the live registry solved via Eureka Server.
3. **Idempotency Guardrail:** The system queries the Redis Cache. If a matching tracking key is currently logged, the execution cycle drops instantly, dropping duplicate network overhead.
4. **Saga Sequence Pipelines:** 
   * **Step 1:** Core banking payment subsystem validates user fund availability.
   * **Step 2:** Relational schemas trigger isolated simultaneous Debit and Credit executions in the MySQL Ledger Table.
   * **Step 3:** The process transitions cleanly, logs notifications, and registers the transaction status as SUCCESS.
5. **Rollback Compensation:** If structural boundaries break at runtime, the exception triggers the central orchestrator catch blocks, reversing account records to original values while archiving a clean audited FAILED tracking profile.
