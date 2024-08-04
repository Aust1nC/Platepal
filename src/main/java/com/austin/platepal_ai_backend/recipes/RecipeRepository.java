package com.austin.platepal_ai_backend.recipes;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RecipeRepository extends MongoRepository<Recipe, String> {

    // TODO: Don't show Recipe that is already in the user's match list
    @Aggregation(pipeline = "{ $sample: { size : 1 } }")
    Recipe getRandomRecipe();
}
