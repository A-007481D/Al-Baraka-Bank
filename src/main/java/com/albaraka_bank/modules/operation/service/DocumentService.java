package com.albaraka_bank.modules.operation.service;

import com.albaraka_bank.modules.ai.model.AiDecision;
import com.albaraka_bank.modules.ai.service.AiService;
import com.albaraka_bank.modules.operation.model.Document;
import com.albaraka_bank.modules.operation.model.Operation;
import com.albaraka_bank.modules.operation.repository.DocumentRepository;
import com.albaraka_bank.modules.operation.repository.OperationRepository;
import com.albaraka_bank.modules.operation.service.storage.StoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final OperationRepository operationRepository;
    private final StoragePort storagePort;

    private final AiService aiService;
    private final OperationService operationService;
    private final com.albaraka_bank.modules.ai.service.TextExtractionService textExtractionService;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_TYPES = List.of("application/pdf", "image/jpeg", "image/png");

    @Transactional
    public Document uploadDocument(Long operationId, MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Only PDF, JPG, and PNG are allowed");
        }

        Operation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new RuntimeException("Operation not found"));

        String storagePath = storagePort.store(file, operationId);

        Document document = Document.builder()
                .fileName(file.getOriginalFilename())
                .fileType(contentType)
                .storagePath(storagePath)
                .operation(operation)
                .build();

        Document savedDoc = documentRepository.save(document);

        try {
            // Extract text from document
            String documentContent = textExtractionService.extractText(file);

            var analysisResult = aiService.analyzeOperation(operation.getAmount().doubleValue(), contentType,
                    documentContent);

            // Save reasoning
            operation.setAiAnalysis(analysisResult.decision() + ": " + analysisResult.reasoning());
            operationRepository.save(operation);

            // Auto-execution disabled per user request. Agent must review.
            System.out.println("AI Analysis completed. Decision: " + analysisResult.decision());
        } catch (Exception e) {
            System.err.println("AI Analysis failed: " + e.getMessage());
            // Save error to analysis field for debugging
            try {
                operation.setAiAnalysis("AI Analysis Failed: " + e.getMessage());
                operationRepository.save(operation);
            } catch (Exception ex) {
                // Ignore
            }
        }

        return savedDoc;
    }
}
