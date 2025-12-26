package com.albaraka_bank.modules.operation.strategy;

import com.albaraka_bank.modules.account.model.Account;
import com.albaraka_bank.modules.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DepositStrategy implements OperationStrategy {

    private final AccountRepository accountRepository;

    @Override
    public void process(BigDecimal amount, Account sourceAccount, Account destinationAccount) {
        if (sourceAccount == null) {
            throw new IllegalArgumentException("Account is required for deposit operations");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }

        sourceAccount.setBalance(sourceAccount.getBalance().add(amount));
        accountRepository.save(sourceAccount);
    }
}

