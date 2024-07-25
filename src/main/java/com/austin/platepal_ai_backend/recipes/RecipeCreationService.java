package com.austin.platepal_ai_backend.recipes;

import org.springframework.ai.openai.OpenAiChatModel;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RecipeCreationService {

    private static final String STABLE_DIFFUSION_URL = "";

    private OpenAiChatModel chatClient;

    private HttpClient httpClient;

    private HttpRequest.Builder stableDiffusionRequestBuilder;

    private List<Recipe> generatedRecipes = new ArrayList<>();

    private static final String RECIPES_FILE_PATH = "recipes.json";

    private RecipeRepository recipeRepository;

    public RecipeCreationService(OpenAiChatModel chatClient, RecipeRepository recipeRepository) {
        this.chatClient = chatClient;
        this.recipeRepository = recipeRepository;
        this.httpClient = HttpClient.newHttpClient();
        this.stableDiffusionRequestBuilder = HttpRequest.newBuilder()
                .setHeader("Content-type", "application/json")
                .uri(URI.create(STABLE_DIFFUSION_URL));
    }

    public static <T> T getRandomElement(List<T> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    public void createRecipes(int numberOfRecipes) {
        List<MealType> mealType = List.of(
                MealType.BREAKFASAT, MealType.LUNCH, MealType.DINNER, MealType.DESSERT, MealType.SNACK
        );
        List<String> cookingTime = List.of(
                "Under 30 Minutes", "Quick and Easy", "One-Pot", "Make-Ahead"
        );
        List<String> ingredientsPool = List.of(
                "Chicken", "Beef", "Pork",
                "Fish", "Pasta", "Rice",
                "Vegetables", "Nuts", "Fruits",
                "Herbs and Spices", "Eggs", "Others"
        );

    }
}
