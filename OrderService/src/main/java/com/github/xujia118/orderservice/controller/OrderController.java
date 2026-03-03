package com.github.xujia118.orderservice.controller;

import com.github.xujia118.orderservice.model.Order;
import com.github.xujia118.orderservice.model.OrderKey;
import com.github.xujia118.orderservice.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Only need accountId to fetch all orders of this account
    @GetMapping("/{accountId}")
    public List<Order> getOrdersByAccount(@PathVariable Long accountId) {
        return orderService.getOrdersByAccount(accountId);
    }

    // Lookup using both parts of the Primary Key
    @GetMapping("/{accountId}/{orderId}")
    public Order getOrder(@PathVariable Long accountId, @PathVariable UUID orderId) {
        OrderKey key = new OrderKey();
        key.setAccountId(accountId);
        key.setOrderId(orderId);
        return orderService.getOrderById(key);
    }

    @PostMapping("/{accountId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Order createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

    @PutMapping("/{accountId}/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public Order updateOrder(@PathVariable Long accountId,
                             @PathVariable UUID orderId,
                             @RequestBody Order updatedOrder) {
        OrderKey key = new OrderKey();
        key.setAccountId(accountId);
        key.setOrderId(orderId);
        return orderService.updateOrder(key, updatedOrder);
    }

    @PutMapping("/{accountId}/{orderId}/cancel")
    @ResponseStatus(HttpStatus.OK) // return 204 for successful deletion
    public Order cancelOrder(@PathVariable Long accountId, @PathVariable UUID orderId) {
        OrderKey key = new OrderKey();
        key.setAccountId(accountId);
        key.setOrderId(orderId);
        return orderService.cancelOrder(key);
    }
}
