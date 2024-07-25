package com.austin.platepal_ai_backend.recipes;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

@Service
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
//        this.stableDiffusionRequestBuilder = HttpRequest.newBuilder()
//                .setHeader("Content-type", "application/json")
//                .uri(URI.create(STABLE_DIFFUSION_URL));
    }

    public static <T> T getRandomElement(List<T> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }


    public void createRecipes(int numberOfRecipes) {
        List<MealType> mealTypes = List.of(
                MealType.BREAKFASAT, MealType.LUNCH, MealType.DINNER, MealType.DESSERT, MealType.SNACK
        );
        List<String> cookingTimes = List.of(
                "Under 30 Minutes", "Quick and Easy", "One-Pot", "Make-Ahead"
        );
        List<String> ingredientsPool = List.of(
                "Chicken", "Beef", "Pork",
                "Fish", "Pasta", "Rice",
                "Vegetables", "Nuts", "Fruits",
                "Herbs and Spices", "Eggs", "Others"
        );
        List<String> cuisines = List.of(
                "Italian", "Greek", "French", "Indian",
                "Mexican", "Thai", "Spanish", "Filipino",
                "Chinese", "Japanese", "Vietnamese", "Korean",
                "Turkish", "Malaysian", "Brazilian", "American"
        );

        while (this.generatedRecipes.size() < numberOfRecipes) {
            MealType mealType = getRandomElement(mealTypes);
            String cookingTime = getRandomElement(cookingTimes);
            String cuisine = getRandomElement(cuisines);
            Set<String> ingredients = new HashSet<>();

            // Generate a random number from 1 to 5;
            int ingredientNumber = (int) ((Math.random() * 4) + 1);

            while (ingredients.size() < ingredientNumber) {
                ingredients.add(getRandomElement(ingredientsPool));
            }

            String prompt = "Create a recipe for a " + cuisine + " (" + mealType
                    + ") that can be prepared in " + cookingTime + ". The ingredients include: " +
                    String.join(", ", ingredients) + ". Include the recipe name, description, " +
                    "and detailed instructions.";

            ChatResponse response = chatClient.call(new Prompt(
                    prompt,
                    OpenAiChatOptions.builder().withFunction("saveRecipe").build()
            ));

            System.out.println(response.getResult().getOutput().getContent());
//            try {
//                ChatResponse response = chatClient.call(new Prompt(
//                        prompt,
//                        OpenAiChatOptions.builder().withFunction("saveRecipe").build()
//                ));
//
//                String result = response.getResult().getOutput().getContent();
//                System.out.println(result);
//
//
//
//            } catch (Exception e) {}

        }
    }

    @Bean
    @Description("Save the generated recipe information") // function description
    public Function<Recipe, Boolean> saveRecipe() {
        return (Recipe recipe) -> {
            System.out.println("Recipe generated: " + recipe);
            this.generatedRecipes.add(recipe);
            return true;
        };
    }
}
