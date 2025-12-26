package com.albaraka_bank.modules.operation.strategy;

import com.albaraka_bank.modules.account.model.Account;
import com.albaraka_bank.modules.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WithdrawalStrategyTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private WithdrawalStrategy withdrawalStrategy;

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .accountNumber("1234567890123456")
                .balance(new BigDecimal("1000"))
                .build();
    }

    @Test
    @DisplayName("Withdrawal should decrease account balance")
    void process_validWithdrawal_decreasesBalance() {
        BigDecimal withdrawalAmount = new BigDecimal("300");
        BigDecimal expectedBalance = new BigDecimal("700");

        withdrawalStrategy.process(withdrawalAmount, account, null);

        assertEquals(expectedBalance, account.getBalance());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    @DisplayName("Withdrawal with null account should throw exception")
    void process_nullAccount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> withdrawalStrategy.process(new BigDecimal("500"), null, null));
    }

    @Test
    @DisplayName("Withdrawal with insufficient balance should throw exception")
    void process_insufficientBalance_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> withdrawalStrategy.process(new BigDecimal("2000"), account, null));
    }

    @Test
    @DisplayName("Withdrawal with zero amount should throw exception")
    void process_zeroAmount_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> withdrawalStrategy.process(BigDecimal.ZERO, account, null));
    }

    @Test
    @DisplayName("Withdrawal with negative amount should throw exception")
    void process_negativeAmount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> withdrawalStrategy.process(new BigDecimal("-100"), account, null));
    }
}
