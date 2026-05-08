package com.financas.tema1;

import com.financas.tema1.application.CategoryStrategy;
import com.financas.tema1.application.TransactionNormalizer;
import com.financas.tema1.domain.Category;
import com.financas.tema1.domain.Transaction;
import com.financas.tema1.domain.User;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionNormalizerTest {

    @Mock
    private CategoryStrategy categoryStrategy;

    @InjectMocks
    private TransactionNormalizer normalizer;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("user@test.com");
    }

    @Test
    @DisplayName("Deve normalizar transação corretamente com todos os campos válidos")
    void shouldNormalizeTransactionSuccessfully() {
        when(categoryStrategy.normalize("food")).thenReturn(Category.FOOD);

        Transaction result = normalizer.normalizeRawData(
                "Almoço restaurante",
                new BigDecimal("45.90"),
                "food",
                "2026-04-15",
                "API1",
                user
        );

        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Almoço restaurante");
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("45.90"));
        assertThat(result.getCategory()).isEqualTo(Category.FOOD);
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2026, 4, 15));
        assertThat(result.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(result.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("Deve sempre criar transação do tipo EXPENSE")
    void shouldAlwaysCreateExpenseType() {
        when(categoryStrategy.normalize(any())).thenReturn(Category.OTHER);

        Transaction result = normalizer.normalizeRawData(
                "Qualquer coisa", new BigDecimal("100.00"), "other", "2026-01-01", "src", user
        );

        assertThat(result.getType()).isEqualTo(TransactionType.EXPENSE);
    }

    @Test
    @DisplayName("Deve delegar normalização de categoria para CategoryStrategy")
    void shouldDelegateCategoryNormalizationToStrategy() {
        when(categoryStrategy.normalize("transporte")).thenReturn(Category.TRANSPORT);

        normalizer.normalizeRawData(
                "Uber", new BigDecimal("25.00"), "transporte", "2026-03-10", "API", user
        );

        verify(categoryStrategy, times(1)).normalize("transporte");
    }

    @Test
    @DisplayName("Deve parsear data no formato yyyy-MM-dd corretamente")
    void shouldParseDateCorrectly() {
        when(categoryStrategy.normalize(any())).thenReturn(Category.OTHER);

        Transaction result = normalizer.normalizeRawData(
                "Teste", new BigDecimal("10.00"), "other", "2025-12-31", "src", user
        );

        assertThat(result.getDate()).isEqualTo(LocalDate.of(2025, 12, 31));
    }

    @Test
    @DisplayName("Deve lançar exceção quando data estiver em formato inválido")
    void shouldThrowExceptionForInvalidDateFormat() {
        assertThatThrownBy(() ->
                normalizer.normalizeRawData(
                        "Teste", new BigDecimal("10.00"), "food", "31/12/2025", "src", user
                )
        ).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Deve normalizar transação com categoria null (delegando ao strategy)")
    void shouldNormalizeWithNullCategory() {
        when(categoryStrategy.normalize(null)).thenReturn(Category.OTHER);

        Transaction result = normalizer.normalizeRawData(
                "Sem categoria", new BigDecimal("50.00"), null, "2026-02-20", "src", user
        );

        assertThat(result.getCategory()).isEqualTo(Category.OTHER);
        verify(categoryStrategy).normalize(null);
    }
}
