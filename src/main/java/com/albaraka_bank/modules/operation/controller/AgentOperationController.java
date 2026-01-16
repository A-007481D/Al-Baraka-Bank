package com.albaraka_bank.modules.operation.controller;

import com.albaraka_bank.modules.operation.dto.OperationResponse;
import com.albaraka_bank.modules.operation.model.Document;
import com.albaraka_bank.modules.operation.repository.DocumentRepository;
import com.albaraka_bank.modules.operation.service.OperationService;
import com.albaraka_bank.modules.iam.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent/operations")
@RequiredArgsConstructor
public class AgentOperationController {

    private final OperationService operationService;
    private final DocumentRepository documentRepository;
    private final JwtService jwtService;

    @GetMapping("/pending")
    public ResponseEntity<List<OperationResponse>> getPendingOperations() {
        List<OperationResponse> operations = operationService.getPendingOperations();
        return ResponseEntity.ok(operations);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<OperationResponse> approveOperation(@PathVariable Long id) {
        OperationResponse response = operationService.approveOperation(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<OperationResponse> rejectOperation(@PathVariable Long id) {
        OperationResponse response = operationService.rejectOperation(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get document info for an operation
     */
    @GetMapping("/{operationId}/document/info")
    public ResponseEntity<?> getDocumentInfo(@PathVariable Long operationId) {
        return documentRepository.findByOperationId(operationId)
                .map(doc -> ResponseEntity.ok(Map.of(
                        "id", doc.getId(),
                        "fileName", doc.getFileName(),
                        "fileType", doc.getFileType(),
                        "uploadedAt", doc.getUploadedAt().toString())))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Download/view document file for an operation
     * Accepts token as query parameter for iframe/new tab viewing
     */
    @GetMapping("/{operationId}/document")
    public ResponseEntity<Resource> getDocument(
            @PathVariable Long operationId,
            @RequestParam(required = false) String token) {

        // Validate token if provided (for iframe/new tab access)
        if (token != null && !token.isEmpty()) {
            try {
                String username = jwtService.extractUsername(token);
                String role = jwtService.extractRole(token);
                if (username == null || !jwtService.isTokenValid(token) || !"AGENT_BANCAIRE".equals(role)) {
                    return ResponseEntity.status(403).build();
                }
            } catch (Exception e) {
                return ResponseEntity.status(403).build();
            }
        }

        Document document = documentRepository.findByOperationId(operationId)
                .orElseThrow(() -> new RuntimeException("Document not found for operation: " + operationId));

        try {
            Path filePath = Paths.get(document.getStoragePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = document.getFileType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Could not read document: " + e.getMessage());
        }
    }
}
