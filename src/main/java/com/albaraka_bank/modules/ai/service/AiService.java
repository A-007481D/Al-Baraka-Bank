package com.albaraka_bank.modules.ai.service;

import com.albaraka_bank.modules.ai.model.AiDecision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final OpenAiChatModel chatModel;

    public AiDecision analyzeOperation(Double amount, String documentType) {
        log.info("AI analyzing operation: amount={}, docType={}", amount, documentType);

        String promptText = """
                You are an AI Risk Analyst for Al Baraka Bank.
                Analyze the following transaction:
                - Amount: {amount} DH
                - Document Type: {documentType}

                Rules:
                1. If amount < 20000 and document is 'payslip', recommend APPROVE.
                2. If amount > 50000, always recommend NEED_HUMAN_REVIEW.
                3. If document is 'suspicious', recommend REJECT.
                4. Otherwise, recommend NEED_HUMAN_REVIEW.

                Respond ONLY with one of these exact words: APPROVE, REJECT, NEED_HUMAN_REVIEW.
                """;

        PromptTemplate template = new PromptTemplate(promptText);
        Prompt prompt = template.create(Map.of(
                "amount", amount,
                "documentType", documentType));

        String response = chatModel.call(prompt).getResult().getOutput().getText().trim();

        try {
            return AiDecision.valueOf(response);
        } catch (IllegalArgumentException e) {
            log.warn("AI returned invalid decision: {}. Defaulting to NEED_HUMAN_REVIEW", response);
            return AiDecision.NEED_HUMAN_REVIEW;
        }
    }
}
