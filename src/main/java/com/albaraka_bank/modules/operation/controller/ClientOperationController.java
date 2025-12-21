package com.albaraka_bank.modules.operation.controller;

import com.albaraka_bank.common.util.SecurityUtils;
import com.albaraka_bank.modules.iam.model.User;
import com.albaraka_bank.modules.operation.dto.OperationRequest;
import com.albaraka_bank.modules.operation.dto.OperationResponse;
import com.albaraka_bank.modules.operation.model.Document;
import com.albaraka_bank.modules.operation.service.DocumentService;
import com.albaraka_bank.modules.operation.service.OperationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/client/operations")
@RequiredArgsConstructor
public class ClientOperationController {

    private final OperationService operationService;
    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<OperationResponse> createOperation(@Valid @RequestBody OperationRequest request) {
        User user = SecurityUtils.getCurrentUser();
        OperationResponse response = operationService.createOperation(request, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/document")
    public ResponseEntity<String> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        Document document = documentService.uploadDocument(id, file);
        return ResponseEntity.ok("Document uploaded: " + document.getFileName());
    }

    @GetMapping
    public ResponseEntity<List<OperationResponse>> getOperations() {
        User user = SecurityUtils.getCurrentUser();
        List<OperationResponse> operations = operationService.getOperationsByUser(user);
        return ResponseEntity.ok(operations);
    }
}
