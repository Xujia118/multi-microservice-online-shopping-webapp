package com.github.xujia118.itemservice.service;

import com.github.xujia118.common.dto.OrderItemDto;
import com.github.xujia118.common.dto.PaymentDto;
import com.github.xujia118.itemservice.entity.Item;
import com.github.xujia118.itemservice.exception.InsufficientStockException;
import com.github.xujia118.itemservice.exception.ItemNotFoundException;
import com.github.xujia118.itemservice.repository.ItemRepository;
import com.mongodb.client.result.UpdateResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ItemService itemService;

    private Item testItem;
    private PaymentDto testPaymentDto;
    private OrderItemDto testOrderItemDto;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId("1");
        testItem.setName("Test Item");
        testItem.setUnitPrice(10.99);
        testItem.setStock(100);
        testItem.setDescription("Test description");

        testOrderItemDto = new OrderItemDto();
        testOrderItemDto.setItemId("1");
        testOrderItemDto.setItemName("Test Item");
        testOrderItemDto.setQuantity(5);
        testOrderItemDto.setPriceAtPurchase(10.99);

        testPaymentDto = new PaymentDto();
        testPaymentDto.setOrderId("order-123");
        testPaymentDto.setAccountId("1");
        testPaymentDto.setTotalAmount(new BigDecimal("54.95"));
        testPaymentDto.setItems(Arrays.asList(testOrderItemDto));
    }

    @Test
    void findAllItems_ShouldReturnAllItems() {
        List<Item> expectedItems = Arrays.asList(testItem);
        when(itemRepository.findAll()).thenReturn(expectedItems);

        List<Item> result = itemService.findAllItems();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testItem, result.get(0));
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    void getItemById_WithValidId_ShouldReturnItem() {
        when(itemRepository.findById("1")).thenReturn(Optional.of(testItem));

        Item result = itemService.getItemById("1");

        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("Test Item", result.getName());
        verify(itemRepository, times(1)).findById("1");
    }

    @Test
    void getItemById_WithInvalidId_ShouldThrowItemNotFoundException() {
        when(itemRepository.findById("999")).thenReturn(Optional.empty());

        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class, () -> {
            itemService.getItemById("999");
        });

        assertEquals("Item not found with id 999", exception.getMessage());
        verify(itemRepository, times(1)).findById("999");
    }

    @Test
    void deductStock_WithSufficientStock_ShouldDeductStock() {
        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.getModifiedCount()).thenReturn(1L);
        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Item.class)))
                .thenReturn(updateResult);

        assertDoesNotThrow(() -> itemService.deductStock(testPaymentDto));

        verify(mongoTemplate, times(1)).updateFirst(any(Query.class), any(Update.class), eq(Item.class));
    }

    @Test
    void deductStock_WithInsufficientStock_ShouldThrowInsufficientStockException() {
        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.getModifiedCount()).thenReturn(0L);
        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Item.class)))
                .thenReturn(updateResult);

        InsufficientStockException exception = assertThrows(InsufficientStockException.class, () -> {
            itemService.deductStock(testPaymentDto);
        });

        assertEquals("Insufficient stock for item Item Test Item is out of stock.", exception.getMessage());
        verify(mongoTemplate, times(1)).updateFirst(any(Query.class), any(Update.class), eq(Item.class));
    }

    @Test
    void deductStock_WithMultipleItems_ShouldProcessAllItems() {
        OrderItemDto secondItem = new OrderItemDto();
        secondItem.setItemId("2");
        secondItem.setItemName("Second Item");
        secondItem.setQuantity(3);
        secondItem.setPriceAtPurchase(15.99);

        PaymentDto paymentWithMultipleItems = new PaymentDto();
        paymentWithMultipleItems.setOrderId("order-456");
        paymentWithMultipleItems.setAccountId("2");
        paymentWithMultipleItems.setTotalAmount(new BigDecimal("109.92"));
        paymentWithMultipleItems.setItems(Arrays.asList(testOrderItemDto, secondItem));

        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.getModifiedCount()).thenReturn(1L);
        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Item.class)))
                .thenReturn(updateResult);

        assertDoesNotThrow(() -> itemService.deductStock(paymentWithMultipleItems));

        verify(mongoTemplate, times(2)).updateFirst(any(Query.class), any(Update.class), eq(Item.class));
    }

    @Test
    void deductStock_WithPartialFailure_ShouldRollbackAndThrowException() {
        OrderItemDto secondItem = new OrderItemDto();
        secondItem.setItemId("2");
        secondItem.setItemName("Second Item");
        secondItem.setQuantity(999); // This will cause insufficient stock
        secondItem.setPriceAtPurchase(15.99);

        PaymentDto paymentWithMultipleItems = new PaymentDto();
        paymentWithMultipleItems.setOrderId("order-789");
        paymentWithMultipleItems.setAccountId("1");
        paymentWithMultipleItems.setTotalAmount(new BigDecimal("109.92"));
        paymentWithMultipleItems.setItems(Arrays.asList(testOrderItemDto, secondItem));

        UpdateResult successResult = mock(UpdateResult.class);
        UpdateResult failureResult = mock(UpdateResult.class);
        when(successResult.getModifiedCount()).thenReturn(1L);
        when(failureResult.getModifiedCount()).thenReturn(0L);

        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Item.class)))
                .thenReturn(successResult)
                .thenReturn(failureResult);

        InsufficientStockException exception = assertThrows(InsufficientStockException.class, () -> {
            itemService.deductStock(paymentWithMultipleItems);
        });

        assertNotNull(exception);
        verify(mongoTemplate, times(2)).updateFirst(any(Query.class), any(Update.class), eq(Item.class));
    }
}
