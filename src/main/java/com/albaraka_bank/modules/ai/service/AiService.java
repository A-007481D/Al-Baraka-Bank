package com.albaraka_bank.modules.ai.service;

import com.albaraka_bank.modules.ai.model.AiAnalysisResult;
import com.albaraka_bank.modules.ai.model.AiDecision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final ChatModel chatModel;

    public AiAnalysisResult analyzeOperation(Double amount, String documentType, String documentContent) {
        log.info("AI analyzing operation: amount={}, docType={}", amount, documentType);

        String promptText = """
                You are an AI Risk Analyst for Al Baraka Bank.
                Analyze the following transaction:
                - Transaction Amount: {amount} DH
                - Document Type: {documentType}
                - Document Content (Extracted Text):
                "{documentContent}"

                Rules:
                1. Verify if the document content supports the transaction amount.
                2. If the document mentions an amount, it MUST match the transaction amount (within 5% tolerance).
                3. If the document is an image or text is unreadable, flag it for human review.
                4. If amount < 50000 and document is 'payslip' OR 'invoice' and content matches amount, recommend APPROVE.
                5. If amount >= 50000, always recommend NEED_HUMAN_REVIEW.
                6. If document is 'suspicious' or content contradicts the amount, recommend REJECT.
                7. If amount >= 20000 and amount < 50000 and content matches, recommend APPROVE.

                Respond in the following format:
                DECISION: [APPROVE|REJECT|NEED_HUMAN_REVIEW]
                REASONING: [One sentence explaining why, citing specific content from the document if possible]
                """;

        PromptTemplate template = new PromptTemplate(promptText);
        Prompt prompt = template.create(Map.of(
                "amount", amount,
                "documentType", documentType,
                "documentContent", documentContent != null ? documentContent : "No content extracted"));

        String response = chatModel.call(prompt).getResult().getOutput().getText().trim();

        try {
            String[] parts = response.split("REASONING:", 2);
            String decisionStr = parts[0].replace("DECISION:", "").trim();
            String reasoning = parts.length > 1 ? parts[1].trim() : "No reasoning provided";

            return new AiAnalysisResult(AiDecision.valueOf(decisionStr), reasoning);
        } catch (Exception e) {
            log.warn("AI returned invalid format: {}. Defaulting to NEED_HUMAN_REVIEW", response);
            return new AiAnalysisResult(AiDecision.NEED_HUMAN_REVIEW,
                    "AI Error: " + response);
        }
    }
}
