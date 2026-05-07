package com.financas.tema1.application;

import com.financas.tema1.domain.Category;
import org.springframework.stereotype.Component;

@Component
public class DefaultCategoryStrategy implements CategoryStrategy {
    @Override
    public Category normalize(String rawCategory) {
        if (rawCategory == null) return Category.OTHER;
        String lower = rawCategory.toLowerCase();
        if (lower.contains("food") || lower.contains("alimentacao") || lower.contains("drink")) {
            return Category.FOOD;
        } else if (lower.contains("transport") || lower.contains("transporte") || lower.contains("uber")) {
            return Category.TRANSPORT;
        } else if (lower.contains("entertain") || lower.contains("lazer") || lower.contains("jogo")) {
            return Category.ENTERTAINMENT;
        }
        return Category.OTHER;
    }
}