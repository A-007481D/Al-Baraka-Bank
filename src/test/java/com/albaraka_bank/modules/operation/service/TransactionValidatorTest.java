package com.albaraka_bank.modules.operation.service;

import com.albaraka_bank.modules.operation.model.OperationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionValidatorTest {

    private TransactionValidator transactionValidator;

    @BeforeEach
    void setUp() {
        transactionValidator = new TransactionValidator();
    }

    @Test
    @DisplayName("Amount <= 10000 should return EXECUTED status")
    void validate_amountBelowThreshold_returnsExecuted() {
        assertEquals(OperationStatus.EXECUTED, transactionValidator.validate(new BigDecimal("5000")));
        assertEquals(OperationStatus.EXECUTED, transactionValidator.validate(new BigDecimal("10000")));
        assertEquals(OperationStatus.EXECUTED, transactionValidator.validate(new BigDecimal("1")));
    }

    @Test
    @DisplayName("Amount > 10000 should return PENDING status")
    void validate_amountAboveThreshold_returnsPending() {
        assertEquals(OperationStatus.PENDING, transactionValidator.validate(new BigDecimal("10001")));
        assertEquals(OperationStatus.PENDING, transactionValidator.validate(new BigDecimal("50000")));
        assertEquals(OperationStatus.PENDING, transactionValidator.validate(new BigDecimal("100000")));
    }

    @Test
    @DisplayName("requiresManualReview returns true for amount > 10000")
    void requiresManualReview_aboveThreshold_returnsTrue() {
        assertTrue(transactionValidator.requiresManualReview(new BigDecimal("10001")));
        assertTrue(transactionValidator.requiresManualReview(new BigDecimal("50000")));
    }

    @Test
    @DisplayName("requiresManualReview returns false for amount <= 10000")
    void requiresManualReview_belowOrEqualThreshold_returnsFalse() {
        assertFalse(transactionValidator.requiresManualReview(new BigDecimal("10000")));
        assertFalse(transactionValidator.requiresManualReview(new BigDecimal("5000")));
    }
}
