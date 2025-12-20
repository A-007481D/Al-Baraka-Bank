package com.albaraka_bank.modules.operation.service;

import com.albaraka_bank.modules.operation.model.OperationStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionValidator {

    private static final BigDecimal THRESHOLD = new BigDecimal("10000");

    public OperationStatus validate(BigDecimal amount) {
        if (amount.compareTo(THRESHOLD) <= 0) {
            return OperationStatus.EXECUTED;
        }
        return OperationStatus.PENDING;
    }

    public boolean requiresManualReview(BigDecimal amount) {
        return amount.compareTo(THRESHOLD) > 0;
    }
}

