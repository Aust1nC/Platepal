package com.austin.platepal_ai_backend.recipes;

import java.util.List;

public record Recipe(
        String id,
        String name,
        String description,
        List<String> ingredients,
        List<String> instructions,
        String cuisine,
        MealType mealType,
        List<String> cookingTime,
        String imageUrl
) {}
