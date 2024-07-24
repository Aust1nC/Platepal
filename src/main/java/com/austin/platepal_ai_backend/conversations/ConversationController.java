package com.austin.platepal_ai_backend.conversations;

import com.austin.platepal_ai_backend.recipes.RecipeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
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

    @GetMapping("/conversations/{conversationId}")
    public Conversation getConversation(@PathVariable("conversationId") String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unable to find conversation with the ID " + conversationId)
                );
    }


    @PostMapping("/conversations")
    public Conversation createNewConversation(@RequestBody CreateConversationRequest request) {

        recipeRepository.findById(request.recipeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unable to find a profile with ID " + request.recipeId()));

        Conversation conversation = new Conversation(
                UUID.randomUUID().toString(),
                request.recipeId(),
                new ArrayList<>()
        );

        conversationRepository.save(conversation);
        return conversation;
    }

    @PutMapping("/conversations/{conversationId}")
    public Conversation addMessageToConversation(
            @PathVariable("conversationId") String conversationId,
            @RequestBody ChatMessage chatMessage
    ) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Unable to find conversation with ID " + conversationId
                ));

        recipeRepository.findById(chatMessage.authorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Unable to find a recipe with ID " + chatMessage.authorId()
                ));

        // TODO: Need to validate that the author of a message happens to be
        //  the only profile associated with the method user

        ChatMessage messageWithTime = new ChatMessage(
                chatMessage.messageText(),
                chatMessage.authorId(),
                LocalDateTime.now()
        );

        conversation.messages().add(messageWithTime);
        conversationRepository.save(conversation);
        return conversation;
    }


    public record CreateConversationRequest (
            String recipeId
    ){}
}
