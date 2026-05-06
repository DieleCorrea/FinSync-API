package com.financas.tema1.transaction;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {

	List<FinancialTransaction> findByUserIdOrderByTransactionDateDesc(Long userId);
}
