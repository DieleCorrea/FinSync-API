package com.financas.tema1.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.financas.tema1.ai.AiInsightResponse;
import com.financas.tema1.domain.Transaction;
import com.financas.tema1.repository.TransactionRepository;
import com.financas.tema1.transaction.TransactionType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class FinancialAiService {

    private final ChatClient chatClient;
    private final TransactionRepository transactionRepository;

    public FinancialAiService(ChatClient.Builder chatClientBuilder,
                              TransactionRepository transactionRepository) {
        this.chatClient = chatClientBuilder.build();
        this.transactionRepository = transactionRepository;
    }

    public AiInsightResponse answer(Long userId, String question) {
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByDateDesc(userId);
        
        // Filtrar últimos 30 dias + normalizar + deduplicar
        final List<Transaction> filteredAndProcessed = deduplicateTransactions(
            normalizeTransactions(
                filterLast30Days(transactions)
            )
        );

        String answer = chatClient.prompt()
                .system("""
               Voce e um analista financeiro do FinSync.
						Responda em portugues do Brasil, de forma objetiva e acionavel.
						Use apenas os dados financeiros fornecidos no contexto.
						Quando faltar informacao, diga quais dados seriam necessarios.
						Nao invente transacoes, saldos ou categorias.
						""")
                .user(user -> user
                        .text("""
								Pergunta do usuario:
								{question}

								Contexto recuperado do sistema, usado como RAG:
								{context}
								""")
                        .param("question", question)
                        .param("context", buildContext(filteredAndProcessed)))
                .call()
                .content();

        return new AiInsightResponse(userId, question, answer);
    }

    private List<Transaction> filterLast30Days(List<Transaction> transactions) {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        return transactions.stream()
                .filter(transaction -> transaction.getDate() != null && 
                        !transaction.getDate().isBefore(thirtyDaysAgo))
                .collect(Collectors.toList());
    }

    private List<Transaction> normalizeTransactions(List<Transaction> transactions) {
        return transactions.stream()
                .peek(transaction -> {
                    if (transaction.getDescription() != null) {
                        transaction.setDescription(transaction.getDescription().trim());
                    }
                })
                .collect(Collectors.toList());
    }

    private List<Transaction> deduplicateTransactions(List<Transaction> transactions) {
        final Set<String> seen = new HashSet<>();
        return transactions.stream()
                .filter(transaction -> {
                    String key = createDeduplicationKey(transaction);
                    return seen.add(key);
                })
                .collect(Collectors.toList());
    }

    private String createDeduplicationKey(Transaction transaction) {
        return String.format("%s|%s|%s|%s|%s",
                transaction.getDate(),
                transaction.getType(),
                transaction.getCategory(),
                transaction.getDescription() != null ? transaction.getDescription().toLowerCase().trim() : "",
                transaction.getAmount());
    }

    private String buildContext(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return "Nenhuma transação cadastrada para este usuário.";
        }

        BigDecimal income = totalByType(transactions, TransactionType.INCOME);
        BigDecimal expenses = totalByType(transactions, TransactionType.EXPENSE);
        BigDecimal balance = income.subtract(expenses);

        StringBuilder context = new StringBuilder();
        context.append("Resumo do usuário:\n");
        context.append("- Total de receitas: ").append(income).append("\n");
        context.append("- Total de despesas: ").append(expenses).append("\n");
        context.append("- Saldo estimado: ").append(balance).append("\n\n");
        context.append("Transações:\n");

        for (Transaction transaction : transactions) {
            context.append("- ")
                    .append(transaction.getDate()) // Campo 'date' da classe Transaction
                    .append(" | ")
                    .append(transaction.getType())
                    .append(" | ")
                    .append(transaction.getCategory())
                    .append(" | ")
                    .append(transaction.getDescription())
                    .append(" | valor: ")
                    .append(transaction.getAmount())
                    .append("\n");
        }

        return context.toString();
    }

    private BigDecimal totalByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(transaction -> type.equals(transaction.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}