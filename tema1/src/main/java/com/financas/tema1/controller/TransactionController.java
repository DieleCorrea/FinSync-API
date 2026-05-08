package com.financas.tema1.controller;

import com.financas.tema1.DTO.TransactionCreateDTO;
import com.financas.tema1.DTO.TransactionDTO;
import com.financas.tema1.domain.Transaction;
import com.financas.tema1.domain.User;
import com.financas.tema1.repository.TransactionRepository;
import com.financas.tema1.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionController(TransactionRepository transactionRepository,
                                 UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    // ─── POST /api/transactions → cria transação ──────────────────
    @PostMapping
    public ResponseEntity<TransactionDTO> create(
            @RequestBody TransactionCreateDTO dto,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        Transaction transaction = new Transaction(
                dto.description(),
                dto.amount(),
                dto.category(),
                dto.date(),
                dto.type(),
                user
        );

        Transaction saved = transactionRepository.save(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
    }

    // ─── GET /api/transactions → extrato completo ─────────────────
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAll(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        List<TransactionDTO> transactions = transactionRepository
                .findByUserIdOrderByDateDesc(user.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(transactions);
    }

    // ─── GET /api/transactions/last30days → últimos 30 dias ───────
    @GetMapping("/last30days")
    public ResponseEntity<List<TransactionDTO>> getLast30Days(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        List<TransactionDTO> transactions = transactionRepository
                .findByUserIdOrderByDateDesc(user.getId())
                .stream()
                .filter(t -> !t.getDate().isBefore(thirtyDaysAgo))
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(transactions);
    }

    // ─── GET /api/transactions/summary → consolidado por categoria ─
    @GetMapping("/summary")
    public ResponseEntity<Map<String, BigDecimal>> getSummary(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        Map<String, BigDecimal> summary = transactionRepository
                .findByUserId(user.getId())
                .stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().toString(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));

        return ResponseEntity.ok(summary);
    }

    // ─── helpers ──────────────────────────────────────────────────
    private User getAuthenticatedUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    private TransactionDTO toDTO(Transaction t) {
        return new TransactionDTO(
                t.getId(),
                t.getDescription(),
                t.getAmount(),
                t.getDate(),
                t.getCategory(),
                t.getType()
        );
    }
}