package com.financas.tema1.ai;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.financas.tema1.transaction.FinancialTransaction;
import com.financas.tema1.transaction.FinancialTransactionRepository;
import com.financas.tema1.transaction.TransactionType;

@Service
public class FinancialAiService {

	private final ChatClient chatClient;
	private final FinancialTransactionRepository transactionRepository;

	public FinancialAiService(ChatClient.Builder chatClientBuilder,
			FinancialTransactionRepository transactionRepository) {
		this.chatClient = chatClientBuilder.build();
		this.transactionRepository = transactionRepository;
	}

	public AiInsightResponse answer(AiInsightRequest request) {
		List<FinancialTransaction> transactions = transactionRepository
				.findByUserIdOrderByTransactionDateDesc(request.userId());

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
						.param("question", request.question())
						.param("context", buildContext(transactions)))
				.call()
				.content();

		return new AiInsightResponse(request.userId(), request.question(), answer);
	}

	private String buildContext(List<FinancialTransaction> transactions) {
		if (transactions.isEmpty()) {
			return "Nenhuma transacao cadastrada para este usuario.";
		}

		BigDecimal income = totalByType(transactions, TransactionType.INCOME);
		BigDecimal expenses = totalByType(transactions, TransactionType.EXPENSE);
		BigDecimal balance = income.subtract(expenses);

		StringBuilder context = new StringBuilder();
		context.append("Resumo do usuario:\n");
		context.append("- Total de receitas: ").append(income).append("\n");
		context.append("- Total de despesas: ").append(expenses).append("\n");
		context.append("- Saldo estimado: ").append(balance).append("\n\n");
		context.append("Transacoes:\n");

		for (FinancialTransaction transaction : transactions) {
			context.append("- ")
					.append(transaction.getTransactionDate())
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

	private BigDecimal totalByType(List<FinancialTransaction> transactions, TransactionType type) {
		return transactions.stream()
				.filter(transaction -> type.equals(transaction.getType()))
				.map(FinancialTransaction::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
