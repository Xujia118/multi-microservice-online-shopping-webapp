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
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void deductStock(PaymentDto paymentDto) {
        try {
            for (OrderItemDto itemDto : paymentDto.getItems()) {
                deductSingleItem(itemDto);
            }
            log.info("Stock deduction successful for Order: {}", paymentDto.getOrderId());

        } catch (InsufficientStockException e) {
            log.error("Insufficient stock. Triggering compensation for Order: {}", paymentDto.getOrderId());

            // Re-throw to trigger @Transactional rollback
            throw e;
        }
    }

    private void deductSingleItem(OrderItemDto itemDto) {
        Query query = new Query(Criteria.where("_id").is(itemDto.getItemId())
                .and("stock").gte(itemDto.getQuantity()));

        Update update = new Update().inc("stock", -itemDto.getQuantity());

        UpdateResult result = mongoTemplate.updateFirst(query, update, Item.class);

        if (result.getModifiedCount() == 0) {
            throw new InsufficientStockException("Item " + itemDto.getItemName() + " is out of stock.");
        }
    }
}
