package com.financas.tema1.controller;

import com.financas.tema1.ai.AiInsightRequest;
import com.financas.tema1.ai.AiInsightResponse;
import com.financas.tema1.domain.User;
import com.financas.tema1.repository.UserRepository;
import com.financas.tema1.service.FinancialAiService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class FinancialAiController {

    private final FinancialAiService financialAiService;
    private final UserRepository userRepository;

    public FinancialAiController(FinancialAiService financialAiService, UserRepository userRepository) {
        this.financialAiService = financialAiService;
        this.userRepository = userRepository;
    }

    @PostMapping("/insights")
    public AiInsightResponse insights(
            @RequestBody AiInsightRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return financialAiService.answer(user.getId(), request.question());
    }
}