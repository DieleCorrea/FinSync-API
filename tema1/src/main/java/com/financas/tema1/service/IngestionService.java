package com.financas.tema1.service;

import com.financas.tema1.application.TransactionNormalizer;
import com.financas.tema1.domain.Transaction;
import com.financas.tema1.domain.User;
import com.financas.tema1.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
@Service
public class IngestionService {

    private final TransactionRepository transactionRepository;
    private final TransactionNormalizer normalizer;

    public IngestionService(TransactionRepository transactionRepository, TransactionNormalizer normalizer) {
        this.transactionRepository = transactionRepository;
        this.normalizer = normalizer;
    }

    public List<Transaction> ingestFromExternalSources(User user) {
        List<Transaction> importedTransactions = java.util.Collections.synchronizedList(new ArrayList<>());

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                BigDecimal amount = new BigDecimal("150.00");
                LocalDate date = LocalDate.parse("2026-04-15");

                if (!isDuplicate("Conta de Luz", amount, date, user)) {
                    Transaction t1 = normalizer.normalizeRawData(
                            "Conta de Luz", amount, "Utilities", date.toString(), "API1", user
                    );
                    saveTransaction(t1);
                    importedTransactions.add(t1);
                }
            });

            executor.submit(() -> {
                BigDecimal amount = new BigDecimal("300.00");
                LocalDate date = LocalDate.parse("2026-04-20");

                if (!isDuplicate("Supermercado", amount, date, user)) {
                    Transaction t2 = normalizer.normalizeRawData(
                            "Supermercado", amount, "Food & Drinks", date.toString(), "API2", user
                    );
                    saveTransaction(t2);
                    importedTransactions.add(t2);
                }
            });
        }
        return importedTransactions;
    }

    private synchronized void saveTransaction(Transaction t) {
        transactionRepository.save(t);
    }

    private boolean isDuplicate(String description, BigDecimal amount, LocalDate date, User user) {
        return transactionRepository.existsByDescriptionAndAmountAndDateAndUser(
                description, amount, date, user);
    }
}