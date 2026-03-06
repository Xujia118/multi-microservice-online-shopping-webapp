package com.github.xujia118.orderservice.service;

import com.github.xujia118.common.model.OrderStatus;
import com.github.xujia118.common.model.PaymentType;
import com.github.xujia118.orderservice.model.Order;
import com.github.xujia118.orderservice.model.OrderItem;
import com.github.xujia118.orderservice.model.OrderKey;
import com.github.xujia118.orderservice.producer.OrderPublisher;
import com.github.xujia118.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderPublisher orderPublisher;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private Long accountId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        accountId = 1L;
        orderId = UUID.randomUUID();

        testOrder = new Order();
        testOrder.setKey(new OrderKey());
        testOrder.getKey().setAccountId(accountId);
        testOrder.getKey().setOrderId(orderId);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setPaymentType(PaymentType.CREDIT_CARD);
        testOrder.setPaymentMethodId(1L);
        testOrder.setItems(Arrays.asList(
            createOrderItem("item1", 2, 10.00),
            createOrderItem("item2", 1, 20.00)
        ));
        testOrder.calculateTotal();
    }

    private OrderItem createOrderItem(String itemId, int quantity, double price) {
        OrderItem item = new OrderItem();
        item.setItemId(itemId);
        item.setQuantity(quantity);
        item.setPriceAtPurchase(price);
        return item;
    }

    @Test
    void getOrdersByAccount_ShouldReturnOrders() {
        List<Order> expectedOrders = Arrays.asList(testOrder);
        when(orderRepository.findByKeyAccountId(accountId)).thenReturn(expectedOrders);

        List<Order> result = orderService.getOrdersByAccount(accountId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder, result.get(0));
        verify(orderRepository, times(1)).findByKeyAccountId(accountId);
    }

    @Test
    void getOrderById_WithValidId_ShouldReturnOrder() {
        when(orderRepository.findById(testOrder.getKey())).thenReturn(Optional.of(testOrder));

        Order result = orderService.getOrderById(accountId, orderId);

        assertNotNull(result);
        assertEquals(testOrder, result);
        verify(orderRepository, times(1)).findById(testOrder.getKey());
    }

    @Test
    void getOrderById_WithInvalidId_ShouldThrowResponseStatusException() {
        when(orderRepository.findById(any(OrderKey.class))).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            orderService.getOrderById(accountId, UUID.randomUUID());
        });

        assertEquals("404 NOT_FOUND \"Order not found\"", exception.getMessage());
        verify(orderRepository, times(1)).findById(any(OrderKey.class));
    }

    @Test
    void createOrder_ShouldCreateAndPublishOrder() {
        Order newOrder = new Order();
        newOrder.setPaymentType(PaymentType.CREDIT_CARD);
        newOrder.setPaymentMethodId(1L);
        newOrder.setItems(Arrays.asList(
            createOrderItem("item1", 2, 10.00)
        ));

        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.createOrder(accountId, newOrder);

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertNotNull(result.getKey());
        assertEquals(accountId, result.getKey().getAccountId());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderPublisher, times(1)).publishOrder(any(Order.class));
    }

    @Test
    void updateOrder_WithPendingOrder_ShouldUpdateAndPublish() {
        Order updatedOrder = new Order();
        updatedOrder.setPaymentType(PaymentType.PAYPAL);
        updatedOrder.setPaymentMethodId(2L);
        updatedOrder.setItems(Arrays.asList(
            createOrderItem("item3", 3, 15.00)
        ));

        when(orderRepository.findById(testOrder.getKey())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.updateOrder(accountId, orderId, updatedOrder);

        assertNotNull(result);
        assertEquals(PaymentType.PAYPAL, result.getPaymentType());
        assertEquals(2L, result.getPaymentMethodId());
        assertEquals(1, result.getItems().size());
        verify(orderRepository, times(1)).findById(testOrder.getKey());
        verify(orderRepository, times(1)).save(testOrder);
        verify(orderPublisher, times(1)).publishOrder(testOrder);
    }

    @Test
    void updateOrder_WithNonPendingOrder_ShouldThrowResponseStatusException() {
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(testOrder.getKey())).thenReturn(Optional.of(testOrder));

        Order updatedOrder = new Order();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            orderService.updateOrder(accountId, orderId, updatedOrder);
        });

        assertEquals("409 CONFLICT \"Order can only be updated while in PENDING status.\"", exception.getMessage());
        verify(orderRepository, times(1)).findById(testOrder.getKey());
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderPublisher, never()).publishOrder(any(Order.class));
    }

    @Test
    void cancelOrder_WithPendingOrder_ShouldCancelAndPublish() {
        when(orderRepository.findById(testOrder.getKey())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.cancelOrder(accountId, orderId);

        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderRepository, times(1)).findById(testOrder.getKey());
        verify(orderRepository, times(1)).save(testOrder);
        verify(orderPublisher, times(1)).publishOrder(testOrder);
    }

    @Test
    void cancelOrder_WithShippedOrder_ShouldThrowResponseStatusException() {
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(testOrder.getKey())).thenReturn(Optional.of(testOrder));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            orderService.cancelOrder(accountId, orderId);
        });

        assertEquals("409 CONFLICT \"Cannot cancel order once it is shipped or delivered.\"", exception.getMessage());
        verify(orderRepository, times(1)).findById(testOrder.getKey());
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderPublisher, never()).publishOrder(any(Order.class));
    }

    @Test
    void cancelOrder_WithDeliveredOrder_ShouldThrowResponseStatusException() {
        testOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(testOrder.getKey())).thenReturn(Optional.of(testOrder));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            orderService.cancelOrder(accountId, orderId);
        });

        assertEquals("409 CONFLICT \"Cannot cancel order once it is shipped or delivered.\"", exception.getMessage());
        verify(orderRepository, times(1)).findById(testOrder.getKey());
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderPublisher, never()).publishOrder(any(Order.class));
    }

    @Test
    void cancelOrder_WithPaidOrder_ShouldCancelAndPublish() {
        testOrder.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(testOrder.getKey())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.cancelOrder(accountId, orderId);

        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderRepository, times(1)).findById(testOrder.getKey());
        verify(orderRepository, times(1)).save(testOrder);
        verify(orderPublisher, times(1)).publishOrder(testOrder);
    }
}
