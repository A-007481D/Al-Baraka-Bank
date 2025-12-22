package com.albaraka_bank.modules.operation.dto;

import com.albaraka_bank.modules.operation.model.OperationStatus;
import com.albaraka_bank.modules.operation.model.OperationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationResponse {

    private Long id;
    private OperationType type;
    private BigDecimal amount;
    private OperationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime validatedAt;
    private LocalDateTime executedAt;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private boolean hasDocument;
}
