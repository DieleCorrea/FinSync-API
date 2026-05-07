package com.financas.tema1.application;

import com.financas.tema1.domain.Category;

public interface CategoryStrategy {
    Category normalize(String rawCategory);
}