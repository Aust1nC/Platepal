package com.austin.platepal_ai_backend;

import com.austin.platepal_ai_backend.conversations.ConversationRepository;
import com.austin.platepal_ai_backend.recipes.MealType;
import com.austin.platepal_ai_backend.recipes.Recipe;
import com.austin.platepal_ai_backend.recipes.RecipeCreationService;
import com.austin.platepal_ai_backend.recipes.RecipeRepository;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootApplication
public class PlatepalAiBackendApplication implements CommandLineRunner {

	@Autowired
	private RecipeRepository recipeRepository;

	@Autowired
	private ConversationRepository conversationRepository;

	@Autowired
	private OpenAiChatModel chatClient;

	@Autowired
	private RecipeCreationService recipeCreationService;

	private static final String BASE_URL = "https://api.stability.ai/v2beta/stable-image/generate/core";
	private static final String KEY = System.getenv("STABILITY_API_KEY");

	public static void main(String[] args) {

		SpringApplication.run(PlatepalAiBackendApplication.class, args);
	}

	public void run(String... args) {

//		try {
//			String prompt = "Lighthouse on a cliff overlooking the ocean";
//			String outputFormat = "jpeg";
//
//			String boundary = UUID.randomUUID().toString();
//			String lineSeparator = "\r\n";
//
//			// Create the multipart form-data body
//			StringBuilder formData = new StringBuilder();
//			formData.append("--").append(boundary).append(lineSeparator)
//					.append("Content-Disposition: form-data; name=\"prompt\"").append(lineSeparator)
//					.append(lineSeparator)
//					.append(prompt).append(lineSeparator)
//					.append("--").append(boundary).append(lineSeparator)
//					.append("Content-Disposition: form-data; name=\"output_format\"").append(lineSeparator)
//					.append(lineSeparator)
//					.append(outputFormat).append(lineSeparator)
//					.append("--").append(boundary).append("--").append(lineSeparator);
//
//
//			HttpClient client = HttpClient.newHttpClient();
//			HttpRequest request = HttpRequest.newBuilder()
//					.uri(URI.create(BASE_URL))
//					.header("Authorization", "Bearer " + KEY)
//					.header("Accept", "image/*")
//					.header("Content-Type", "multipart/form-data; boundary=" + boundary)
//					.POST(HttpRequest.BodyPublishers.ofString(formData.toString()))
//					.build();
//
//			HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
//
//			if (response.statusCode() == 200) {
//				Files.write(Paths.get("src/main/resources/recipeImages/lighthouse.jpeg"), response.body());
//				System.out.println("Image saved successfully.");
//			} else {
//				System.err.println("Error: " + response.statusCode() + ": " + new String(response.body()));
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();;
//		}


		this.recipeCreationService.createRecipes(1);


//		recipeRepository.deleteAll();
//		conversationRepository.deleteAll();
//
//		Recipe recipe = new Recipe(
//				"1",
//				"Fish and Chips",
//				"A British Classic",
//				List.of("Fish", "Potato"),
//				List.of("Fried Fish", "Fried Potato"),
//				"British",
//				MealType.DINNER,
//				List.of("classic", "fried", "British"),
//				"http://example.com/fish-and-chips.jpg"
//		);
//		recipeRepository.save(recipe);
//		Recipe recipe2 = new Recipe(
//				"2",
//				"Roast Beef",
//				"A British Classic",
//				List.of("Beef"),
//				List.of("Roast the beef"),
//				"British",
//				MealType.DINNER,
//				List.of("classic", "British"),
//				"http://example.com/roast-beef.jpg"
//		);
//		recipeRepository.save(recipe2);
//		recipeRepository.findAll().forEach(System.out::println);

	}

}
