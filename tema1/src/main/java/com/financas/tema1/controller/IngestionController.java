package com.financas.tema1.controller;

import com.financas.tema1.DTO.TransactionDTO;
import com.financas.tema1.domain.Transaction;
import com.financas.tema1.domain.User;
import com.financas.tema1.repository.UserRepository;
import com.financas.tema1.service.IngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ingest")
public class IngestionController {

    private final IngestionService ingestionService;
    private final UserRepository userRepository;

    public IngestionController(IngestionService ingestionService,
                               UserRepository userRepository) {
        this.ingestionService = ingestionService;
        this.userRepository = userRepository;
    }

    // POST /api/ingest → dispara o pipeline de ingestão
    @PostMapping
    public ResponseEntity<List<TransactionDTO>> ingest(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<Transaction> imported = ingestionService.ingestFromExternalSources(user);

        List<TransactionDTO> result = imported.stream()
                .map(t -> new TransactionDTO(
                        t.getId(),
                        t.getDescription(),
                        t.getAmount(),
                        t.getDate(),
                        t.getCategory(),
                        t.getType()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}