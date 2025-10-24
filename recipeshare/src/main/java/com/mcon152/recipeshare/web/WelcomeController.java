package com.mcon152.recipeshare.web;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// SOLID: SRP — Only returns a welcome message over HTTP.
// Keep non-HTTP work out of controllers.
@RestController
public class WelcomeController {
    @GetMapping("/")
    public String home() {
        return "Welcome to RecipeShare!";
    }

    @GetMapping("/hello")
    public String hello() { return "RecipeShare is up!"; }
}
