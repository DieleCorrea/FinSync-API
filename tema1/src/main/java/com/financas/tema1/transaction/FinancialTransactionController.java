package com.financas.tema1.transaction;

import java.util.List;

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

	public FinancialTransactionController(FinancialTransactionRepository repository) {
		this.repository = repository;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	FinancialTransaction create(@RequestBody FinancialTransaction transaction) {
		return repository.save(transaction);
	}

	@GetMapping("/user/{userId}")
	List<FinancialTransaction> listByUser(@PathVariable Long userId) {
		return repository.findByUserIdOrderByTransactionDateDesc(userId);
	}
}
