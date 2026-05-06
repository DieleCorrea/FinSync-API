package com.financas.tema1.application;

import com.financas.tema1.domain.Transaction;
import com.financas.tema1.domain.User;
import com.financas.tema1.infrastructure.TransactionRepository;
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
        List<Transaction> importedTransactions = new ArrayList<>();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                if (!isDuplicate("Conta de Luz", new BigDecimal("150.00"), user)) {
                    Transaction t1 = normalizer.normalizeRawData(
                            "Conta de Luz", new BigDecimal("150.00"), "Utilities", "2026-04-15", "API1", user
                    );
                    saveTransaction(t1);
                    importedTransactions.add(t1);
                }
            });

            executor.submit(() -> {
                if (!isDuplicate("Supermercado", new BigDecimal("300.00"), user)) {
                    Transaction t2 = normalizer.normalizeRawData(
                            "Supermercado", new BigDecimal("300.00"), "Food & Drinks", "2026-04-20", "API2", user
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

    private boolean isDuplicate(String description, BigDecimal amount, User user) {
        return transactionRepository.existsByDescriptionAndAmountAndDateAndUser(
                description, amount, LocalDate.now(), user
        );
    }
}