package com.financas.tema1.DTO;

import com.financas.tema1.domain.Category;
import com.financas.tema1.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionDTO(
        Long id,
        String description,
        BigDecimal amount,
        LocalDate date,
        Category category,
        TransactionType type
) {}