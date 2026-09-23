package com.paycore.paymentservice.saga;

public enum SagaStatus {
    STARTED,
    PAYMENT_COMPLETED,
    LEDGER_UPDATED,
    NOTIFICATION_SENT,
    FAILED,
    COMPENSATING,
    COMPENSATED
}