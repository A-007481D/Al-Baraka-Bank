package com.albaraka_bank.modules.operation.factory;

import com.albaraka_bank.modules.operation.model.OperationType;
import com.albaraka_bank.modules.operation.strategy.DepositStrategy;
import com.albaraka_bank.modules.operation.strategy.OperationStrategy;
import com.albaraka_bank.modules.operation.strategy.TransferStrategy;
import com.albaraka_bank.modules.operation.strategy.WithdrawalStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OperationFactory {

    private final DepositStrategy depositStrategy;
    private final WithdrawalStrategy withdrawalStrategy;
    private final TransferStrategy transferStrategy;

    public OperationStrategy getStrategy(OperationType type) {
        return switch (type) {
            case DEPOSIT -> depositStrategy;
            case WITHDRAWAL -> withdrawalStrategy;
            case TRANSFER -> transferStrategy;
        };
    }
}

