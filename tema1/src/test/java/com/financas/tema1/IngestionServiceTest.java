package com.financas.tema1;

import com.financas.tema1.application.TransactionNormalizer;
import com.financas.tema1.domain.Category;
import com.financas.tema1.domain.Transaction;
import com.financas.tema1.domain.User;
import com.financas.tema1.repository.TransactionRepository;
import com.financas.tema1.service.IngestionService;
import com.financas.tema1.transaction.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionNormalizer normalizer;

    @InjectMocks
    private IngestionService ingestionService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("user@test.com");
    }

    @Test
    @DisplayName("Deve ingerir 2 transações quando não houver duplicatas")
    void shouldIngestTwoTransactionsWhenNoDuplicates() {
        when(transactionRepository.existsByDescriptionAndAmountAndDateAndUser(
                anyString(), any(), any(), any())).thenReturn(false);

        Transaction t1 = buildTransaction("Conta de Luz", "150.00", Category.OTHER);
        Transaction t2 = buildTransaction("Supermercado", "300.00", Category.FOOD);

        when(normalizer.normalizeRawData(eq("Conta de Luz"), any(), any(), any(), any(), any()))
                .thenReturn(t1);
        when(normalizer.normalizeRawData(eq("Supermercado"), any(), any(), any(), any(), any()))
                .thenReturn(t2);

        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Transaction> result = ingestionService.ingestFromExternalSources(user);

        assertThat(result).hasSize(2);
        verify(transactionRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("Deve ignorar transações duplicadas")
    void shouldSkipDuplicateTransactions() {
        when(transactionRepository.existsByDescriptionAndAmountAndDateAndUser(
                anyString(), any(), any(), any())).thenReturn(true);

        List<Transaction> result = ingestionService.ingestFromExternalSources(user);

        assertThat(result).isEmpty();
        verify(transactionRepository, never()).save(any());
        verify(normalizer, never()).normalizeRawData(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve ingerir somente transações não duplicadas quando há mistura")
    void shouldIngestOnlyNonDuplicates() {
        when(transactionRepository.existsByDescriptionAndAmountAndDateAndUser(
                eq("Conta de Luz"), any(), any(), any())).thenReturn(true);
        when(transactionRepository.existsByDescriptionAndAmountAndDateAndUser(
                eq("Supermercado"), any(), any(), any())).thenReturn(false);

        Transaction t2 = buildTransaction("Supermercado", "300.00", Category.FOOD);
        when(normalizer.normalizeRawData(eq("Supermercado"), any(), any(), any(), any(), any()))
                .thenReturn(t2);
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Transaction> result = ingestionService.ingestFromExternalSources(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Supermercado");
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve verificar duplicata com os valores corretos de Conta de Luz")
    void shouldCheckDuplicateWithCorrectValues_ContaDeLuz() {
        when(transactionRepository.existsByDescriptionAndAmountAndDateAndUser(
                anyString(), any(), any(), any())).thenReturn(true);

        ingestionService.ingestFromExternalSources(user);

        verify(transactionRepository).existsByDescriptionAndAmountAndDateAndUser(
                eq("Conta de Luz"),
                eq(new BigDecimal("150.00")),
                eq(LocalDate.of(2026, 4, 15)),
                eq(user)
        );
    }

    @Test
    @DisplayName("Deve verificar duplicata com os valores corretos de Supermercado")
    void shouldCheckDuplicateWithCorrectValues_Supermercado() {
        when(transactionRepository.existsByDescriptionAndAmountAndDateAndUser(
                anyString(), any(), any(), any())).thenReturn(true);

        ingestionService.ingestFromExternalSources(user);

        verify(transactionRepository).existsByDescriptionAndAmountAndDateAndUser(
                eq("Supermercado"),
                eq(new BigDecimal("300.00")),
                eq(LocalDate.of(2026, 4, 20)),
                eq(user)
        );
    }

    private Transaction buildTransaction(String description, String amount, Category category) {
        Transaction t = new Transaction();
        t.setDescription(description);
        t.setAmount(new BigDecimal(amount));
        t.setCategory(category);
        t.setDate(LocalDate.now());
        t.setType(TransactionType.EXPENSE);
        t.setUser(user);
        return t;
    }
}
