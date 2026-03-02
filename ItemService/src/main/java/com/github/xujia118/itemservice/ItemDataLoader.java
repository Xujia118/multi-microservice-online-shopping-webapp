package com.github.xujia118.itemservice;

import com.github.xujia118.itemservice.entity.Item;
import com.github.xujia118.itemservice.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ItemDataLoader implements CommandLineRunner {

    private final ItemRepository itemRepository;

    @Override
    public void run(String... args) {
        if (itemRepository.count() == 0) {
            itemRepository.saveAll(List.of(
                    new Item(null, "iPhone 15 Pro", "A17 Pro chip, Titanium design", 999.00, 50, null),
                    new Item(null, "MacBook Air M3", "13-inch, 8GB RAM, 256GB SSD", 1099.00, 30, null),
                    new Item(null, "iPad Pro", "M2 chip, 11-inch Liquid Retina display", 799.00, 20, null),
                    new Item(null, "Apple Watch Series 9", "Midnight Aluminum Case, S9 chip", 399.00, 100, null),
                    new Item(null, "AirPods Pro (2nd Gen)", "Active Noise Cancellation, USB-C", 249.00, 200, null)
            ));
            System.out.println("Item Service: Apple devices seeded successfully into MongoDB.");
        }
    }
}
