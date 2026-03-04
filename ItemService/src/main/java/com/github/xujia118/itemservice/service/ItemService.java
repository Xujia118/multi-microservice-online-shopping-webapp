package com.github.xujia118.itemservice.service;

import com.github.xujia118.common.dto.OrderItemDto;
import com.github.xujia118.common.dto.PaymentDto;
import com.github.xujia118.itemservice.entity.Item;
import com.github.xujia118.itemservice.exception.InsufficientStockException;
import com.github.xujia118.itemservice.exception.ItemNotFoundException;
import com.github.xujia118.itemservice.repository.ItemRepository;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final MongoTemplate mongoTemplate;

    public List<Item> findAllItems() {
        return itemRepository.findAll();
    }

    public Item getItemById(String id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));

    }

    public void deductStock(PaymentDto paymentDto) {
        log.info("Starting stock deduction for order: {}", paymentDto.getOrderId());

        // Loop through the enriched items list
        for (OrderItemDto itemDto : paymentDto.getItems()) {
            String productId = itemDto.getItemId();
            int quantityToDeduct = itemDto.getQuantity();

            log.debug("Deducting {} units from item {}", quantityToDeduct, productId);

            // We find the item AND ensure it has enough stock in one operation
            Query query = new Query(Criteria.where("_id").is(productId)
                    .and("stock").gte(quantityToDeduct));

            Update update = new Update().inc("stock", -quantityToDeduct);

            UpdateResult result = mongoTemplate.updateFirst(query, update, Item.class);

            if (result.getModifiedCount() == 0) {
                log.error("Insufficient stock or item not found for ID: {}", productId);
                // In a real system, you would trigger a compensating transaction (Refund) here
                throw new InsufficientStockException("Could not deduct stock for: " + itemDto.getItemName());
            }
        }
        log.info("Inventory successfully updated for Order: {}", paymentDto.getOrderId());
    }
}
