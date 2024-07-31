package com.austin.platepal_ai_backend;

import com.austin.platepal_ai_backend.conversations.ConversationRepository;
import com.austin.platepal_ai_backend.recipes.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import java.util.*;

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

	private static final String API_NINJA_URL = "https://api.api-ninjas.com/v1/recipe";
	private static final String API_NINJAS_KEY = System.getenv("API_NINJAS_KEY");


	public static void main(String[] args) {

		SpringApplication.run(PlatepalAiBackendApplication.class, args);
	}

	public void run(String... args) {
		List<String> cuisines = List.of(
				"Italian", "Greek", "French", "Indian",
				"Mexican", "Thai", "Spanish", "Filipino",
				"Chinese", "Japanese", "Vietnamese", "Korean",
				"Turkish", "Malaysian", "Brazilian", "American"
		);

		for (String cuisine : cuisines) {
			this.recipeCreationService.fetchRawRecipes(cuisine);
		}



//		this.recipeCreationService.createRecipes(1);


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
