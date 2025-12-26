package com.albaraka_bank.modules.operation.controller;

import com.albaraka_bank.modules.operation.dto.OperationResponse;
import com.albaraka_bank.modules.operation.service.OperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/keycloak/agent/operations")
@RequiredArgsConstructor
public class KeycloakAgentOperationController {

    private final OperationService operationService;

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
}
