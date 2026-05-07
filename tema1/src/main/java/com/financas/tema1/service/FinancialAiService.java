package com.financas.tema1.service;

import java.math.BigDecimal;
import java.util.List;

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
                        .param("context", buildContext(transactions)))
                .call()
                .content();

        return new AiInsightResponse(userId, question, answer);
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