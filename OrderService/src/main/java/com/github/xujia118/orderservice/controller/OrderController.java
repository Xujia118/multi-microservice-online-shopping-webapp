package com.github.xujia118.orderservice.controller;

import com.github.xujia118.orderservice.model.Order;
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
    @GetMapping()
    public List<Order> getOrdersByAccount(@RequestHeader("X-User-Id") Long accountId) {
        return orderService.getOrdersByAccount(accountId);
    }

    // Lookup using both parts of the Primary Key
    @GetMapping("/{orderId}")
    public Order getOrder(@RequestHeader("X-User-Id") Long accountId, @PathVariable UUID orderId) {
        return orderService.getOrderById(accountId, orderId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order createOrder(@RequestHeader("X-User-Id") Long accountId, @RequestBody Order order) {
        return orderService.createOrder(accountId, order);
    }

    @PutMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public Order updateOrder(@RequestHeader("X-User-Id") Long accountId,
                             @PathVariable UUID orderId,
                             @RequestBody Order updatedOrder) {
        return orderService.updateOrder(accountId, orderId, updatedOrder);
    }

    @PutMapping("/{orderId}/cancel")
    @ResponseStatus(HttpStatus.OK) // return 204 for successful deletion
    public Order cancelOrder(@RequestHeader("X-User-Id") Long accountId, @PathVariable UUID orderId) {
        return orderService.cancelOrder(accountId, orderId);
    }
}
