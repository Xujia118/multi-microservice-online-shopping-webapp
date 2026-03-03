package com.github.xujia118.itemservice.service;

import com.github.xujia118.itemservice.entity.Item;
import com.github.xujia118.itemservice.exception.InsufficientStockException;
import com.github.xujia118.itemservice.exception.ItemNotFoundException;
import com.github.xujia118.itemservice.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public List<Item> findAllItems() {
        return itemRepository.findAll();
    }

    public Item getItemById(String id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));

    }

    public void deductStock(String orderId) {


        Item item = getItemById(id);

        if (item.getStock() < quantity) {
            throw new InsufficientStockException(item.getName());
        }

        item.setStock(item.getStock() - quantity);
        itemRepository.save(item);
    }
}
