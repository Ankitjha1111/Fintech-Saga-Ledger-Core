package com.paycore.paymentservice.service;

import com.paycore.paymentservice.entity.IdempotencyRecord;
import com.paycore.paymentservice.model.Payment;
import com.paycore.paymentservice.repository.IdempotencyRepository;
import com.paycore.paymentservice.repository.PaymentRepository;
import com.paycore.paymentservice.saga.PaymentSagaOrchestrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentSagaOrchestrator paymentSagaOrchestrator;

    public Payment processPayment(Payment payment, String idempotencyKey) {

        // 1. Transaction ID check and auto-generation if missing
        if (payment.getTransactionId() == null || payment.getTransactionId().trim().isEmpty()) {
            payment.setTransactionId(UUID.randomUUID().toString());
        }

        // 2. Check if Idempotency-Key already exists in DB
        if (idempotencyRepository.existsById(idempotencyKey)) {
            throw new RuntimeException("Duplicate Request Blocked! Key already processed: " + idempotencyKey);
        }

        // Save initial processing state
        IdempotencyRecord record = new IdempotencyRecord(idempotencyKey, "PROCESSING");
        idempotencyRepository.save(record);

        try {
            // 3. Trigger the Saga Orchestrator workflow
            paymentSagaOrchestrator.executeSaga(payment.getTransactionId(), payment.getAmount());

            // 4. On Success: Populate details and save payment
            payment.setStatus("SUCCESS");
            payment.setIdempotencyKey(idempotencyKey);
            payment.setCreatedAt(LocalDateTime.now());

            Payment savedPayment = paymentRepository.save(payment);

            record.setStatus("COMPLETED");
            idempotencyRepository.save(record);

            return savedPayment;

        } catch (Exception e) {
            e.printStackTrace();

            // On Failure: Mark status as failed and persist
            record.setStatus("FAILED");
            idempotencyRepository.save(record);

            payment.setStatus("FAILED");
            payment.setIdempotencyKey(idempotencyKey);
            payment.setCreatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            throw new RuntimeException("Payment failed and rolled back: " + e.getMessage());
        }
    }
}