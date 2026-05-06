package com.financas.tema1.application;

import com.financas.tema1.domain.Category;
import com.financas.tema1.domain.Transaction;
import com.financas.tema1.domain.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class TransactionNormalizer {

    private final CategoryStrategy categoryStrategy;

    public TransactionNormalizer(CategoryStrategy categoryStrategy) {
        this.categoryStrategy = categoryStrategy;
    }

    public Transaction normalizeRawData(String rawDescription, BigDecimal rawAmount, String rawCategory, String rawDate, String source, User user) {
        Category category = categoryStrategy.normalize(rawCategory);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(rawDate, formatter);

        return new Transaction(rawDescription, rawAmount, category, date, source, user);
    }
}