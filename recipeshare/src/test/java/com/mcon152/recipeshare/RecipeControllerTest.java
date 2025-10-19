package com.mcon152.recipeshare;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcon152.recipeshare.web.RecipeController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper();
    }

    // Internal class for creation-related tests
    @Nested
    class CreationTests {
        @Test
        void shouldAddNewRecipeSuccessfully() throws Exception {

            ObjectNode json = mapper.createObjectNode();
            json.put("title", "Cake");
            json.put("description", "Delicious cake");
            // Change ingredients to a single String
            json.put("ingredients", "1 cup of flour, 1 cup of sugar, 3 eggs");
            json.put("instructions", "Mix and bake");
            String jsonString = mapper.writeValueAsString(json);
            mockMvc.perform(post("/api/recipes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Cake"))
                    .andExpect(jsonPath("$.description").value("Delicious cake"))
                    .andExpect(jsonPath("$.ingredients").value("1 cup of flour, 1 cup of sugar, 3 eggs"))
                    .andExpect(jsonPath("$.instructions").value("Mix and bake"))
                    .andExpect(jsonPath("$.id").isNumber());
        }

        @ParameterizedTest
        @CsvSource({
                "'Chocolate Cake','Rich chocolate cake','2 cups flour;1 cup cocoa;4 eggs','Bake at 350F for 30 min'",
                "'Pasta Salad','Fresh pasta salad','200g pasta;100g tomatoes;50g olives','Mix all ingredients'",
                "'Pancakes','Fluffy pancakes','1 cup flour;2 eggs;1 cup milk','Cook on skillet until golden'"
        })
        void shouldAddMultipleRecipesWithDifferentInputs(String title, String description, String ingredients, String instructions) throws Exception {
            ObjectNode json = mapper.createObjectNode();
            json.put("title", title);
            json.put("description", description);
            json.put("ingredients", ingredients);
            json.put("instructions", instructions);

            mockMvc.perform(post("/api/recipes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(json)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value(title))
                    .andExpect(jsonPath("$.description").value(description))
                    .andExpect(jsonPath("$.ingredients").value(ingredients))
                    .andExpect(jsonPath("$.instructions").value(instructions))
                    .andExpect(jsonPath("$.id").isNumber());
         }
    }

    // Internal class for delete and get tests
    @Nested
    class DeleteAndGetTests {
        private List<Integer> recipeIds;

        @BeforeEach
        void createRecipes() throws Exception {
            recipeIds = new ArrayList<>();
            String[] recipes = {
                    "{\"title\":\"Pie\",\"description\":\"Apple pie\",\"ingredients\":\"Apples, Flour, Sugar\",\"instructions\":\"Mix and bake\"}",
                    "{\"title\":\"Soup\",\"description\":\"Tomato soup\",\"ingredients\":\"Tomatoes, Water, Salt\",\"instructions\":\"Boil and blend\"}"
            };
            for (String json : recipes) {
                String response = mockMvc.perform(post("/api/recipes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();
                int id = mapper.readTree(response).get("id").asInt();
                recipeIds.add(id);
            }
        }

        @Test
        void shouldReturnAllRecipesWhenGetAllIsCalled() throws Exception {
            mockMvc.perform(get("/api/recipes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Pie"))
                    .andExpect(jsonPath("$[1].title").value("Soup"));
        }

        @Test
        void shouldReturnSingleRecipeById() throws Exception {
            int id = recipeIds.getFirst();
            mockMvc.perform(get("/api/recipes/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Pie"));
        }

        @Test
        void shouldDeleteRecipeAndReturnTrueThenEmptyBodyOnGet() throws Exception {

            // use an existing id created in @BeforeEach
            int id = recipeIds.getFirst();

            // DELETE should succeed and return true
            mockMvc.perform(delete("/api/recipes/" + id))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));

            // verify - GET on the same id should return empty body
            mockMvc.perform(get("/api/recipes/" + id))
                    .andExpect(status().isOk())
                    .andExpect(content().string(""));

        }

        @Test
        void shouldUpdateRecipeCompletelyWithPutRequest() throws Exception {

            // use an existing id created in @BeforeEach
            int id = recipeIds.getFirst();

            ObjectNode json = mapper.createObjectNode();
            json.put("title", "Updated Pie");
            json.put("description", "Apple pie with cinnamon");
            json.put("ingredients", "Apples, Flour, Sugar, Cinnamon");
            json.put("instructions", "Mix well and bake 45 minutes");

            mockMvc.perform(put("/api/recipes/" + id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(json)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated Pie"))
                    .andExpect(jsonPath("$.description").value("Apple pie with cinnamon"))
                    .andExpect(jsonPath("$.ingredients").value("Apples, Flour, Sugar, Cinnamon"))
                    .andExpect(jsonPath("$.instructions").value("Mix well and bake 45 minutes"))
                    .andExpect(jsonPath("$.id").value(id));
        }

        @Test
        void shouldUpdateRecipePartiallyWithPatchRequest() throws Exception {

            // patch the first recipe created in @BeforeEach
            int id = recipeIds.getFirst();

            ObjectNode patch = mapper.createObjectNode();
            patch.put("title", "Grandma's Pie"); // only change title

            mockMvc.perform(patch("/api/recipes/" + id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(patch)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Grandma's Pie"))
                    // unchanged fields from the seed data
                    .andExpect(jsonPath("$.description").value("Apple pie"))
                    .andExpect(jsonPath("$.ingredients").value("Apples, Flour, Sugar"))
                    .andExpect(jsonPath("$.instructions").value("Mix and bake"))
                    .andExpect(jsonPath("$.id").value(id));
        }
    }

    @Nested
    class NonExistingRecipeTests {

        @Test
        void shouldReturnEmptyBodyWhenGettingNonExistingRecipe() throws Exception {
            // Try to get a recipe that doesn't exist
            mockMvc.perform(get("/api/recipes/999999"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(""));
        }

        @Test
        void shouldReturnEmptyBodyWhenUpdatingNonExistingRecipe() throws Exception {
            // Try to update a recipe that doesn't exist
            var body = mapper.createObjectNode();
            body.put("title", "N/A");
            body.put("description", "N/A");
            body.put("ingredients", "N/A");
            body.put("instructions", "N/A");

            mockMvc.perform(put("/api/recipes/999999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(""));
        }

        @Test
        void shouldReturnEmptyBodyWhenPatchingNonExistingRecipe() throws Exception {
            // Try to partially update a recipe that doesn't exist
            var patch = mapper.createObjectNode();
            patch.put("title", "Yummy Cake");

            mockMvc.perform(patch("/api/recipes/999999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(patch)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(""));
        }
    }
}