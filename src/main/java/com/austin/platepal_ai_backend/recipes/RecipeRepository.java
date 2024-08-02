package com.austin.platepal_ai_backend.recipes;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RecipeRepository extends MongoRepository<Recipe, String> {

    @Aggregation(pipeline = "{ $sample: { size : 1 } }")
    Recipe getRandomRecipe();
}
