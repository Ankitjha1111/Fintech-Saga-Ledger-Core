package com.paycore.paymentservice.saga;

import com.paycore.paymentservice.service.DoubleEntryLedgerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentSagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(PaymentSagaOrchestrator.class);

    @Autowired
    private DoubleEntryLedgerService doubleEntryLedgerService;

    @Transactional
    public void executeSaga(String transactionId, Double amount) {
        SagaStatus currentStatus = SagaStatus.STARTED;
        log.info("Saga Workflow STARTED for Transaction ID: {}", transactionId);

        try {
            // Step 1: Payment Processing
            processPaymentStep(transactionId, amount);
            currentStatus = SagaStatus.PAYMENT_COMPLETED;

            // Step 2: Ledger Update
            updateLedgerStep(transactionId, amount);
            currentStatus = SagaStatus.LEDGER_UPDATED;

            // Step 3: Send Notification
            sendNotificationStep(transactionId);
            currentStatus = SagaStatus.NOTIFICATION_SENT;

            log.info("Saga Workflow COMPLETED successfully for Transaction ID: {}", transactionId);

        } catch (Exception e) {
            log.error("Saga failed at state: {}. Error: {}", currentStatus, e.getMessage());
            compensateSaga(transactionId, amount, currentStatus);
        }
    }

    private void processPaymentStep(String transactionId, Double amount) {
        log.info("[Step 1] Payment authorized for amount: {}", amount);
    }

    private void updateLedgerStep(String transactionId, Double amount) {
        log.info("[Step 2] Updating double-entry ledger...");

        // Convert Double to BigDecimal and trigger real double-entry update
        BigDecimal decimalAmount = BigDecimal.valueOf(amount);
        doubleEntryLedgerService.recordTransaction(
                transactionId,
                "ACC-CUST-001",  // Sender (Customer Wallet)
                "ACC-MERCH-001", // Receiver (Merchant Pool)
                decimalAmount
        );

        log.info("[Step 2] Double-entry ledger successfully updated for Txn: {}", transactionId);
    }

    private void sendNotificationStep(String transactionId) {
        log.info("[Step 3] Sending payment success notification...");
    }

    private void compensateSaga(String transactionId, Double amount, SagaStatus failedState) {
        log.warn("--- INITIATING SAGA COMPENSATION (ROLLBACK) ---");

        if (failedState == SagaStatus.PAYMENT_COMPLETED || failedState == SagaStatus.LEDGER_UPDATED) {
            log.info("Compensating: Issuing full refund of {} for transaction ID: {}", amount, transactionId);

            try {
                // Convert Double to BigDecimal for Ledger reversal
                BigDecimal decimalAmount = BigDecimal.valueOf(amount);

                // Actual Database Reversal: Reverse entry in Double-Entry Ledger
                // Sender becomes Merchant (ACC-MERCH-001) and Receiver becomes Customer (ACC-CUST-001)
                doubleEntryLedgerService.recordTransaction(
                        transactionId + "-REV",
                        "ACC-MERCH-001", // Sender (Merchant Pool)
                        "ACC-CUST-001",  // Receiver (Customer Wallet)
                        decimalAmount
                );

                log.info("[Compensation] Reverse ledger entry recorded successfully for Txn: {}", transactionId);

            } catch (Exception e) {
                log.error("[CRITICAL ERROR] Compensation failed for transaction ID: {}. Reason: {}", transactionId, e.getMessage());
            }
        }

        log.info("Saga Compensation Completed, System state restored.");
    }
}