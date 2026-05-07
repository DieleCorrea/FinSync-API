package com.financas.tema1.ai;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class FinancialAiController {

	private final FinancialAiService financialAiService;

	public FinancialAiController(FinancialAiService financialAiService) {
		this.financialAiService = financialAiService;
	}

	@PostMapping("/insights")
	AiInsightResponse insights(@RequestBody AiInsightRequest request) {
		return financialAiService.answer(request);
	}
}
