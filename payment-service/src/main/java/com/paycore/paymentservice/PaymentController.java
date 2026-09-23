package com.paycore.paymentservice.controller;

import com.paycore.paymentservice.model.Payment;
import com.paycore.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/status")
    public String paymentServiceStatus() {
        return "Payment Service is up and running";
    }

    @PostMapping("/pay")
    public Payment makePayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody Payment payment) {
        return paymentService.processPayment(payment, idempotencyKey);
    }
}