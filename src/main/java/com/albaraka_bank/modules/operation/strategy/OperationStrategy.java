package com.albaraka_bank.modules.operation.strategy;

import com.albaraka_bank.modules.account.model.Account;
import java.math.BigDecimal;

public interface OperationStrategy {
    void process(BigDecimal amount, Account sourceAccount, Account destinationAccount);
}

