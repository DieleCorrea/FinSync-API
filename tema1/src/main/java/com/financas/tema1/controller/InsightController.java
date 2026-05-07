package com.financas.tema1.controller;

import com.financas.tema1.application.IngestionService;
import com.financas.tema1.domain.User;
import com.financas.tema1.infrastructure.UserRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/insights")
public class InsightController {

    private final ChatClient chatClient;
    private final IngestionService ingestionService;
    private final UserRepository userRepository;

    public InsightController(ChatClient.Builder builder, IngestionService ingestionService, UserRepository userRepository) {
        this.chatClient = builder.build();
        this.ingestionService = ingestionService;
        this.userRepository = userRepository;
    }

    @PostMapping("/resume")
    public ResponseEntity<String> getSummary(
            @RequestBody Map<String, String> requestData,
            Authentication authentication) {

        String email = authentication != null && authentication.getName() != null
                ? authentication.getName()
                : "usuario@teste.com";

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setPassword("encoded_password");
            return userRepository.save(newUser);
        });

        ingestionService.ingestFromExternalSources(user);

        String prompt = "Resuma o padrão de consumo para o usuário com base nos seguintes dados financeiros: " + requestData.get("dados");


        String response = chatClient.prompt(prompt).call().content();

        return ResponseEntity.ok(response);
    }
}