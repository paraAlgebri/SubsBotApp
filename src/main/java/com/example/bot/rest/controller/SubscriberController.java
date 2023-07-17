package com.example.bot.rest.controller;


import com.example.bot.rest.model.Subscriber;
import com.example.bot.service.SubscribersService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("subscribers")
@Data
public class SubscriberController {

    private final SubscribersService subscribersService;

    @GetMapping
    public List<Subscriber> getAll() {
        return subscribersService.getAll();
    }

    @GetMapping("expired")
    public List<Subscriber> expired() {
        return subscribersService.getExpired();
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable(value = "id") Long id) {
        subscribersService.remove(id);
    }

    @PostMapping
    public Subscriber add(@RequestBody Subscriber subscriber) {
        return subscribersService.add(subscriber);
    }




}