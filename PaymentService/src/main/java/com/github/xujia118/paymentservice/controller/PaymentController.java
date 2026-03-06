package com.github.xujia118.paymentservice.controller;

import com.github.xujia118.paymentservice.model.Payment;
import com.github.xujia118.paymentservice.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@AllArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public List<Payment> getPaymentByAccount(@RequestHeader("X-User-Id") Long accountId) {
        return paymentService.getPaymentByAccount(accountId);
    }
}
