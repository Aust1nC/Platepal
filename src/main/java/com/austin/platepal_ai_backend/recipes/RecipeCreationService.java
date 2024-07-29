package com.austin.platepal_ai_backend.recipes;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.micrometer.common.util.StringUtils;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

@Service
public class RecipeCreationService {

    private static final String STABILITY_URL = System.getenv("STABILITY_URL");

    private static final String STABILITY_KEY = System.getenv("STABILITY_API_KEY");

    private OpenAiChatModel chatClient;

    private HttpClient httpClient;

    private List<Recipe> generatedRecipes = new ArrayList<>();

    private static final String RECIPES_FILE_PATH = "recipes.json";

    private RecipeRepository recipeRepository;

    public RecipeCreationService(OpenAiChatModel chatClient, RecipeRepository recipeRepository) {
        this.chatClient = chatClient;
        this.recipeRepository = recipeRepository;
        this.httpClient = HttpClient.newHttpClient();
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
                    "exact spices and herbs if used, " + "and detailed instructions.";

            ChatResponse response = chatClient.call(new Prompt(
                    prompt,
                    OpenAiChatOptions.builder().withFunction("saveRecipe").build()
            ));

            System.out.println(response.getResult().getOutput().getContent());
        }
        saveRecipesToJson(this.generatedRecipes);
    }

    private void saveRecipesToJson(List<Recipe> generatedRecipes) {
    try {
        Gson gson = new Gson();
        List<Recipe> existingRecipes = gson.fromJson(
                new FileReader(RECIPES_FILE_PATH),
                new TypeToken<ArrayList<Recipe>>() {}.getType()
        );
        if (existingRecipes == null) {
            existingRecipes = new ArrayList<>();
        }

        generatedRecipes.addAll(existingRecipes);

        List<Recipe> recipesWithImages = new ArrayList<>();
        for (Recipe recipe : generatedRecipes) {
            if (recipe.imageUrl() == null || recipe.imageUrl().isEmpty()) {
                recipe = generateRecipeImage(recipe);
            }
            recipesWithImages.add(recipe);
        }
        String jsonString = gson.toJson(recipesWithImages);
        FileWriter writer = new FileWriter(RECIPES_FILE_PATH);
        writer.write(jsonString);
        writer.close();
    } catch (IOException e) {
        throw new RuntimeException(e);
        }
    }

    private Recipe generateRecipeImage(Recipe recipe) {
        String uuid = StringUtils.isBlank(recipe.id()) ? UUID.randomUUID().toString() : recipe.id();
        recipe = new Recipe(
                uuid,
                recipe.name(),
                recipe.description(),
                recipe.ingredients(),
                recipe.instructions(),
                recipe.cuisine(),
                recipe.mealType(),
                recipe.cookingTime(),
                uuid + ".jpg"
        );

        try {
            String prompt = "Picture of a delicious " + recipe.name() + " with ingredients "
                    + String.join(",", recipe.ingredients()) + ". Photorealistic texture and details," +
                    "highly detailed, hyperrealistic, subsurface scattering, 4k DSLR, best quality, masterpiece.";
            String outputFormat = "jpeg";
            String boundary = UUID.randomUUID().toString();
            String lineSeparator = "\r\n";

            // Create the multipart form-data body
            StringBuilder formData = new StringBuilder();
            formData.append("--").append(boundary).append(lineSeparator)
                    .append("Content-Disposition: form-data; name=\"prompt\"").append(lineSeparator)
                    .append(lineSeparator)
                    .append(prompt).append(lineSeparator)
                    .append("--").append(boundary).append(lineSeparator)
                    .append("Content-Disposition: form-data; name=\"output_format\"").append(lineSeparator)
                    .append(lineSeparator)
                    .append(outputFormat).append(lineSeparator)
                    .append("--").append(boundary).append("--").append(lineSeparator);

            System.out.println("Creating image for " + recipe.name());

            // Make a POST request to the Stability URL
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(STABILITY_URL))
                    .header("Authorization", "Bearer " + STABILITY_KEY)
                    .header("Accept", "image/*")
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofString(formData.toString()))
                    .build();

            HttpResponse<byte[]> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                Files.write(Paths.get("src/main/resources/recipeImages/" + recipe.id() + ".jpeg"), response.body());
                System.out.println("Image saved successfully");
            } else {
                System.err.println("Error: " + response.statusCode() + ": " + new String(response.body()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return recipe;
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
