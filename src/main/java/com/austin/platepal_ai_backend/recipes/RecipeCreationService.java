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

import java.io.FileNotFoundException;
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

    private static final String API_NINJA_BASE_URL = System.getenv("API_NINJAS_URL");

    private static final String API_NINJAS_KEY = System.getenv("API_NINJAS_KEY");

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

    private static <T> T getRandomElement(List<T> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    public void fetchRawRecipes(String cuisineType) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_NINJA_BASE_URL + "?query=" + cuisineType))
                    .header("Accept", "application/json")
                    .header("x-api-key", API_NINJAS_KEY)
                    .build();

            HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Fetched cuisine " + cuisineType);

            Gson gson = new Gson();
            List<RawRecipe> rawRecipes = gson.fromJson(response.body(),
                    new TypeToken<List<RawRecipe>>() {}.getType());

            List<Recipe> newRecipes = new ArrayList<>();

            for (RawRecipe raw: rawRecipes) {
                newRecipes.add(mapRawRecipeToRecipe(raw, cuisineType));
            }

            saveRecipesToJsonFile(newRecipes);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveRecipesToJsonFile(List<Recipe> newRecipes) {
        try {
            Gson gson = new Gson();
            List<Recipe> existingRecipes = gson.fromJson(
                    new FileReader(RECIPES_FILE_PATH),
                    new TypeToken<ArrayList<Recipe>>() {}.getType()
            );
            if (existingRecipes == null) {
                existingRecipes = new ArrayList<>();
            }

            // Create a set to track recipe names to prevent duplicates
            Set<String> recipeNames = new HashSet<>();

            for (Recipe recipe : existingRecipes) {
                recipeNames.add(recipe.name().toLowerCase());
            }

            // Add new recipes if their names are not already in the set
            for (Recipe newRecipe : newRecipes) {
                if (!recipeNames.contains(newRecipe.name().toLowerCase())) {
                    existingRecipes.add(newRecipe);
                    // Update the set with the new recipe name
                    recipeNames.add(newRecipe.name().toLowerCase());
                }
            }

            List<Recipe> recipesWithMealTypeAndDescription = new ArrayList<>();
            for (Recipe recipe : existingRecipes) {
                Recipe updatedRecipe;
                if (recipe.description() == null || recipe.description().isEmpty()
                        || recipe.type() == null || recipe.type().isEmpty()) {
                    updatedRecipe = generateRecipeDescriptionAndType(recipe);
                } else {
                    updatedRecipe = recipe;
                }
                recipesWithMealTypeAndDescription.add(updatedRecipe);
            }


            List<Recipe> recipesWithImages = new ArrayList<>();
            for (Recipe recipe: recipesWithMealTypeAndDescription) {
                Recipe recipeWithImage;
                if (recipe.imageUrl() == null || recipe.imageUrl().isEmpty()) {
                    recipeWithImage = generateRecipeImage(recipe);
                } else {
                    recipeWithImage = recipe;
                }
                recipesWithImages.add(recipeWithImage);
            }

            System.out.println("Saving to JSON");

            FileWriter fileWriter = new FileWriter(RECIPES_FILE_PATH);
            fileWriter.write(gson.toJson(recipesWithImages));
            fileWriter.close();

            System.out.println("Finished");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Recipe generateRecipeDescriptionAndType(Recipe recipe) {
        String prompt = "Recipe: " + recipe.name() + "\n" + "Ingredients: " + recipe.ingredients() + "\n"
                + "Instructions: " + recipe.instructions() + "\n\n" + "Please provide a description and identify " +
                "the type of meal (e.g., breakfast, lunch, dinner, snack) for this recipe. The response should " +
                "follow the below format: " + "\n" + "Type of Meal: " + "\n" + "Description:";

        ChatResponse response = chatClient.call(new Prompt(
                prompt,
                OpenAiChatOptions.builder().withFunction("saveRecipe").build()
        ));
        String content = response.getResult().getOutput().getContent();

        String mealType = extractOpenAIResponse(content, "Type of Meal:");
        String mealDescription = extractOpenAIResponse(content, "Description:");


        return new Recipe(
                recipe.id(),
                recipe.name(),
                mealDescription,
                recipe.ingredients(),
                recipe.instructions(),
                mealType,
                recipe.cuisine(),
                recipe.servings(),
                recipe.imageUrl()
        );
    }

    private String extractOpenAIResponse(String content, String field) {
        int startIndex = content.indexOf(field) + field.length();
        int endIndex = content.indexOf("\n", startIndex);
        if (endIndex == -1) {
            endIndex = content.length();
        }
        return content.substring(startIndex, endIndex).trim();
    }

    private Recipe mapRawRecipeToRecipe(RawRecipe raw, String cuisine) {
        String id = "";
        String name = raw.getTitle();
        String description = "";
        List<String> ingredients = Arrays.asList(raw.getIngredients().split("\\|"));
        List<String> instructions = Arrays.asList(raw.getInstructions().split("\\."));
        String type = "";
        String servings = raw.getServings();
        String imageUrl = "";

        return new Recipe(id, name, description, ingredients, instructions, type, cuisine, servings, imageUrl);
    }


    public void createRecipes(int numberOfRecipes) {
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
            String cuisine = getRandomElement(cuisines);
            Set<String> ingredients = new HashSet<>();

            // Generate a random number from 1 to 5;
            int ingredientNumber = (int) ((Math.random() * 4) + 1);

            while (ingredients.size() < ingredientNumber) {
                ingredients.add(getRandomElement(ingredientsPool));
            }

            String prompt = "Create a recipe for a " + cuisine + ". The ingredients include: " +
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
                recipe.type(),
                recipe.cuisine(),
                recipe.servings(),
                uuid + ".jpg"
        );

        System.out.println("Recipe with image: " + recipe);

        try {
            String prompt = "Picture of a delicious " + recipe.name() + " with ingredients "
                    + String.join(",", recipe.ingredients()) + ". Focus on the ingredients and plating." +
                    " Photorealistic texture and details," +
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
