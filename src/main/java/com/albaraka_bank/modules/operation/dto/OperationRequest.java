package com.albaraka_bank.modules.operation.dto;

import com.albaraka_bank.modules.operation.model.OperationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OperationRequest {

    @NotNull(message = "Type is required")
    private OperationType type;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String destinationAccountNumber;
}
