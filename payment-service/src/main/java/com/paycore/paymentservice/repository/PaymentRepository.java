package com.paycore.paymentservice.repository;

import com.paycore.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Transaction ID se find karne ke liye
    Payment findByTransactionId(String transactionId);

    // Idempotency Key se find karne ke liye (Duplicate requests rokne ke liye)
    Payment findByIdempotencyKey(String idempotencyKey);
}