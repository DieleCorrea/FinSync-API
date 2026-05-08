package com.financas.tema1.repository;

import com.financas.tema1.domain.Transaction;
import com.financas.tema1.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUser(User user);

    boolean existsByDescriptionAndAmountAndDateAndUser(
            String description,
            BigDecimal amount,
            LocalDate date,
            User user
    );

    List<Transaction> findByUserIdOrderByDateDesc(Long userId);

    List<Transaction> findByUserId(Long userId); // ← adiciona esse
}