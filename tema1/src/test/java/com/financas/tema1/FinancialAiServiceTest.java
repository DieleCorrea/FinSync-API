package com.financas.tema1;

import com.financas.tema1.ai.AiInsightResponse;
import com.financas.tema1.domain.Category;
import com.financas.tema1.domain.Transaction;
import com.financas.tema1.domain.User;
import com.financas.tema1.repository.TransactionRepository;
import com.financas.tema1.service.FinancialAiService;
import com.financas.tema1.transaction.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialAiServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callSpec;

    private FinancialAiService financialAiService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("user@test.com");

        when(chatClientBuilder.build()).thenReturn(chatClient);

        financialAiService = new FinancialAiService(chatClientBuilder, transactionRepository);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
    }

    @Test
    @DisplayName("Deve retornar AiInsightResponse com userId e pergunta corretos")
    void shouldReturnInsightWithCorrectUserIdAndQuestion() {
        when(transactionRepository.findByUserIdOrderByDateDesc(1L))
                .thenReturn(Collections.emptyList());
        when(callSpec.content()).thenReturn("Você não possui transações ainda.");

        AiInsightResponse response = financialAiService.answer(1L, "Qual meu saldo?");

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.question()).isEqualTo("Qual meu saldo?");
        assertThat(response.answer()).isEqualTo("Você não possui transações ainda.");
    }

    @Test
    @DisplayName("Deve consultar transações do usuário correto")
    void shouldQueryTransactionsForCorrectUser() {
        when(transactionRepository.findByUserIdOrderByDateDesc(42L))
                .thenReturn(Collections.emptyList());
        when(callSpec.content()).thenReturn("Sem dados.");

        financialAiService.answer(42L, "Tenho gastos com lazer?");

        verify(transactionRepository).findByUserIdOrderByDateDesc(42L);
    }

    @Test
    @DisplayName("Deve funcionar com lista de transações vazia")
    void shouldHandleEmptyTransactionList() {
        when(transactionRepository.findByUserIdOrderByDateDesc(any()))
                .thenReturn(Collections.emptyList());
        when(callSpec.content()).thenReturn("Nenhuma transação encontrada.");

        AiInsightResponse response = financialAiService.answer(1L, "Alguma pergunta?");

        assertThat(response.answer()).isNotBlank();
    }

    @Test
    @DisplayName("Deve processar lista com transações de diferentes tipos")
    void shouldHandleTransactionsWithDifferentTypes() {
        Transaction income = buildTransaction("Salário", "5000.00", TransactionType.INCOME, Category.OTHER);
        Transaction expense = buildTransaction("Almoço", "50.00", TransactionType.EXPENSE, Category.FOOD);

        when(transactionRepository.findByUserIdOrderByDateDesc(1L))
                .thenReturn(List.of(income, expense));
        when(callSpec.content()).thenReturn("Seu saldo estimado é R$ 4950,00.");

        AiInsightResponse response = financialAiService.answer(1L, "Qual meu saldo?");

        assertThat(response.answer()).contains("4950");
    }

    private Transaction buildTransaction(String description, String amount,
                                          TransactionType type, Category category) {
        Transaction t = new Transaction();
        t.setDescription(description);
        t.setAmount(new BigDecimal(amount));
        t.setType(type);
        t.setCategory(category);
        t.setDate(LocalDate.now());
        t.setUser(user);
        return t;
    }
}
