package com.austin.platepal_ai_backend.conversations;

import com.austin.platepal_ai_backend.recipes.Recipe;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConversationService {

    private OpenAiChatModel chatModel;

    public ConversationService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public Conversation generateRecipeResonse(Conversation conversation, Recipe recipe) {
        // System message
        String systemMessageStr = String.format(
                        "Name: %s\n" +
                        "Description: %s\n" +
                        "Ingredients: %s\n" +
                        "Instructions: %s\n" +
                        "Type: %s\n" +
                        "Cuisine: %s\n" +
                        "Servings: %s\n" +
                        "This is an in-app text conversation between the recipe and the user." +
                        "Pretend to be the provided recipe and respond to the conversation as informative as possible.",
                recipe.name(),
                recipe.description(),
                String.join(", ", recipe.ingredients()),
                String.join(", ", recipe.instructions()),
                recipe.type(),
                recipe.cuisine(),
                recipe.servings()
        );
        SystemMessage systemMessage = new SystemMessage(systemMessageStr);

        // User or Assistant message
        List<AbstractMessage> conversationMessages = conversation.messages().stream().map(message -> {
            if (message.authorId().equals(recipe.id())) {
                return new AssistantMessage(message.messageText());
            } else {
               return new UserMessage(message.messageText());
            }
        }).toList();

        List<Message> allMessages = new ArrayList<>();
        allMessages.add(systemMessage);
        allMessages.addAll(conversationMessages);

        Prompt prompt = new Prompt(String.valueOf(allMessages));
        ChatResponse response = chatModel.call(prompt);

        conversation.messages().add(new ChatMessage(
                response.getResult().getOutput().getContent(),
                recipe.id(),
                LocalDateTime.now()
        ));

        return conversation;
    }
}