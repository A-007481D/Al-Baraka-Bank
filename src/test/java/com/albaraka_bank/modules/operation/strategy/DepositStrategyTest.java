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
class DepositStrategyTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private DepositStrategy depositStrategy;

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
    @DisplayName("Deposit should increase account balance")
    void process_validDeposit_increasesBalance() {
        BigDecimal depositAmount = new BigDecimal("500");
        BigDecimal expectedBalance = new BigDecimal("1500");

        depositStrategy.process(depositAmount, account, null);

        assertEquals(expectedBalance, account.getBalance());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    @DisplayName("Deposit with null account should throw exception")
    void process_nullAccount_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> depositStrategy.process(new BigDecimal("500"), null, null));
    }

    @Test
    @DisplayName("Deposit with zero amount should throw exception")
    void process_zeroAmount_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> depositStrategy.process(BigDecimal.ZERO, account, null));
    }

    @Test
    @DisplayName("Deposit with negative amount should throw exception")
    void process_negativeAmount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> depositStrategy.process(new BigDecimal("-100"), account, null));
    }
}
