package com.mcon152.recipeshare.web;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// SOLID: SRP — Single, clear purpose: simple HTTP greeting.
// No business logic or data access here.
@RestController
public class HelloController {
    @GetMapping("/")
    public String home() {
        return "Welcome to RecipeShare!";
    }

    @GetMapping("/hello")
    public String hello() { return "RecipeShare is up!"; }
}
