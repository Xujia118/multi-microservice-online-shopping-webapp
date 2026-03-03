package com.github.xujia118.paymentservice.controller;

import com.github.xujia118.paymentservice.model.Order;
import com.github.xujia118.paymentservice.model.Payment;
import com.github.xujia118.paymentservice.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@AllArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Payment Service!";
    }

    @PostMapping()
    public void processOrder(Order order) {
        paymentService.processOrder(order);
    }
}
