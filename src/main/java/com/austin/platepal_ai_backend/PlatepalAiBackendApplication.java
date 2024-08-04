package com.austin.platepal_ai_backend;

import com.austin.platepal_ai_backend.conversations.ConversationRepository;
import com.austin.platepal_ai_backend.matches.MatchRepository;
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
	private MatchRepository matchRepository;

	@Autowired
	private RecipeCreationService recipeCreationService;

	@Autowired
	private ConversationRepository conversationRepository;

	public static void main(String[] args) {

		SpringApplication.run(PlatepalAiBackendApplication.class, args);
	}

	public void run(String... args) {
		clearAllData();
//		this.recipeCreationService.saveRecipesToDB();
	}

	private void clearAllData(){
		matchRepository.deleteAll();
		conversationRepository.deleteAll();
	}
}
