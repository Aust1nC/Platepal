package com.austin.platepal_ai_backend.matches;


import com.austin.platepal_ai_backend.recipes.Recipe;

public record Match(
        String id,
        Recipe recipe,
        String conversationId
) {}
