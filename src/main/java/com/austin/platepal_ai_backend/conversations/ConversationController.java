package com.austin.platepal_ai_backend.conversations;

import com.austin.platepal_ai_backend.recipes.RecipeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.UUID;

@RestController
public class ConversationController {

    private final ConversationRepository conversationRepository;
    private final RecipeRepository recipeRepository;

    public ConversationController(ConversationRepository conversationRepository, RecipeRepository recipeRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.recipeRepository = recipeRepository;
    }

    @PostMapping("/conversations")
    public Conversation createNewConversation(@RequestBody CreateConversationRequest request) {

        recipeRepository.findById(request.recipeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Conversation conversation = new Conversation(
                UUID.randomUUID().toString(),
                request.recipeId(),
                new ArrayList<>()
        );

        conversationRepository.save(conversation);
        return conversation;
    }

    public record CreateConversationRequest (
            String recipeId
    ){}
}
