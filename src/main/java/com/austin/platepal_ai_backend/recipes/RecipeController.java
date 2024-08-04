package com.austin.platepal_ai_backend.recipes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecipeController {

    @Autowired
    private RecipeRepository recipeRepository;

    @CrossOrigin(origins = "*")
    @GetMapping("/recipes/random")
    public Recipe getRandomRecipe() {
        return recipeRepository.getRandomRecipe();
    }
}
