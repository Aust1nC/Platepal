package com.austin.platepal_ai_backend;

import com.austin.platepal_ai_backend.conversations.ConversationRepository;
import com.austin.platepal_ai_backend.recipes.Category;
import com.austin.platepal_ai_backend.recipes.Recipe;
import com.austin.platepal_ai_backend.recipes.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class PlatepalAiBackendApplication implements CommandLineRunner {

	@Autowired
	private RecipeRepository recipeRepository;

	@Autowired
	private ConversationRepository conversationRepository;

	public static void main(String[] args) {

		SpringApplication.run(PlatepalAiBackendApplication.class, args);
	}

	public void run(String... args) {

		recipeRepository.deleteAll();
		conversationRepository.deleteAll();

		Recipe recipe = new Recipe(
				"1",
				"Fish and Chips",
				"A British Classic",
				List.of("Fish", "Potato"),
				List.of("Fried Fish", "Fried Potato"),
				"British",
				Category.DINNER,
				List.of("classic", "fried", "British"),
				"http://example.com/fish-and-chips.jpg"
		);
		recipeRepository.save(recipe);
		Recipe recipe2 = new Recipe(
				"2",
				"Roast Beef",
				"A British Classic",
				List.of("Beef"),
				List.of("Roast the beef"),
				"British",
				Category.DINNER,
				List.of("classic", "British"),
				"http://example.com/roast-beef.jpg"
		);
		recipeRepository.save(recipe2);
		recipeRepository.findAll().forEach(System.out::println);

	}

}
