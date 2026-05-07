package com.financas.tema1.infrastructure;

import com.financas.tema1.domain.Transaction;
import com.financas.tema1.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByUser(User user);
    boolean existsByDescriptionAndAmountAndDateAndUser(String description, java.math.BigDecimal amount, java.time.LocalDate date, User user);
}