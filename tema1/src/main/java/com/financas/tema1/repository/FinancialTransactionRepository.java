package com.financas.tema1.repository;

import java.util.List;

import com.financas.tema1.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialTransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByDateDesc(Long userId);

    List<Transaction> findByUserId(Long userId);
}
