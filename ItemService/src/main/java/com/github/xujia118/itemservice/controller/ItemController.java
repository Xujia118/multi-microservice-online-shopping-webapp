package com.github.xujia118.itemservice.controller;

import com.github.xujia118.itemservice.entity.Item;
import com.github.xujia118.itemservice.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping()
    public List<Item> getAllItems() {
        return itemService.findAllItems();
    }
    @GetMapping("/{id}")
    public Item getItem(@PathVariable String id) {
        return itemService.getItemById(id);
    }

//    @PutMapping("/{id}/inventory")
//    public void deductInventory(@PathVariable String id, @RequestParam int quantity) {
//        itemService.deductStock(id, quantity);
//    }
}
