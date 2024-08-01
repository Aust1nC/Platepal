package com.austin.platepal_ai_backend;

import com.austin.platepal_ai_backend.recipes.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PlatepalAiBackendApplication implements CommandLineRunner {

	@Autowired
	private RecipeRepository recipeRepository;

	@Autowired
	private RecipeCreationService recipeCreationService;

	public static void main(String[] args) {

		SpringApplication.run(PlatepalAiBackendApplication.class, args);
	}

	public void run(String... args) {
//		recipeCreationService.saveRecipesToDB();

//		System.out.println(recipeRepository.findAll().getFirst());
	}

}
