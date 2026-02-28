package com.github.xujia118.itemservice.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/items")
@AllArgsConstructor
public class ItemController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Item Service!";
    }
}
