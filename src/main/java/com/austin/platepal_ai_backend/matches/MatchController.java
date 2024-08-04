package com.austin.platepal_ai_backend.matches;

import com.austin.platepal_ai_backend.conversations.Conversation;
import com.austin.platepal_ai_backend.conversations.ConversationRepository;
import com.austin.platepal_ai_backend.recipes.Recipe;
import com.austin.platepal_ai_backend.recipes.RecipeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import java.util.UUID;

@RestController
public class MatchController {

    private final ConversationRepository conversationRepository;
    private final RecipeRepository recipeRepository;
    private final MatchRepository matchRepository;

    public MatchController(MatchRepository matchRepository, ConversationRepository conversationRepository,
                           RecipeRepository recipeRepository) {
        this.conversationRepository = conversationRepository;
        this.recipeRepository = recipeRepository;
        this.matchRepository = matchRepository;
    }

    public record CreateMatchRequest(String recipeId) {}

    @CrossOrigin(origins = "*")
    @PostMapping("/matches")
    public Match createMatch(@RequestBody CreateMatchRequest request) {
        Recipe recipe = recipeRepository.findById(request.recipeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unable to find a recipe with ID " + request.recipeId()
                ));

        // TODO: Make sure there are no existing conversations with this recipe already
        Conversation conversation = new Conversation(
                UUID.randomUUID().toString(),
                recipe.id(),
                new ArrayList<>()
        );
        conversationRepository.save(conversation);

        Match match =  new Match(
                UUID.randomUUID().toString(),
                recipe,
                conversation.id()
        );
        System.out.println(match);
        matchRepository.save(match);

        return match;
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/matches")
    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

}
