package com.austin.platepal_ai_backend.conversations;

import com.austin.platepal_ai_backend.recipes.Recipe;

import java.util.List;

public record Conversation(
        String id,
        String recipeId,
        List<ChatMessage> messages
) {
}
