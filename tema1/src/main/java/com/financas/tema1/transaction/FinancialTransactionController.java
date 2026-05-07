package com.financas.tema1.transaction;

import java.time.LocalDate;
import java.util.List;

import com.financas.tema1.domain.Category;
import com.financas.tema1.domain.Transaction;
import com.financas.tema1.domain.User;
import com.financas.tema1.repository.FinancialTransactionRepository;
import com.financas.tema1.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class FinancialTransactionController {

	private final FinancialTransactionRepository repository;
    private final UserRepository userRepository;

	public FinancialTransactionController(FinancialTransactionRepository repository, UserRepository userRepository) {
		this.repository = repository;
        this.userRepository = userRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction create(@RequestBody java.util.Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());

        User user = userRepository.findById(String.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Transaction transaction = new Transaction(
                (String) payload.get("description"),
                new java.math.BigDecimal(payload.get("amount").toString()),
                Category.valueOf((String) payload.get("category")),
                LocalDate.parse((String) payload.get("date")),
                TransactionType.valueOf((String) payload.get("type")),
                user
        );

        return repository.save(transaction);
    }

	@GetMapping("/user/{userId}")
	List<Transaction> listByUser(@PathVariable Long userId) {
		return repository.findByUserIdOrderByDateDesc(userId);
	}
}
