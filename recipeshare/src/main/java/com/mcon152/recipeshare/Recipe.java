// MVC: MODEL
// This class is part of the Model layer.
// It just stores recipe information (id, title, description, etc.).
// It does NOT handle user requests or talk to the database yet.

package com.mcon152.recipeshare;

/* SOLID: Single Responsibility — This class only holds recipe data (id, title, etc.).
   It doesn’t handle HTTP or business logic — just a data model.
   SOLID: Liskov Substitution — If a subclass extends Recipe,
   it should keep the same behavior (same getters/setters, no new exceptions). */
public class Recipe {
    private Long id;
    private String title;
    private String description;
    private String ingredients;
    private String instructions;

    // Constructors
    public Recipe() {}

    public Recipe(Long id, String title, String description, String ingredients, String instructions) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}
