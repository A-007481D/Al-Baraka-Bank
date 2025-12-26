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
class TransferStrategyTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransferStrategy transferStrategy;

    private Account sourceAccount;
    private Account destinationAccount;

    @BeforeEach
    void setUp() {
        sourceAccount = Account.builder()
                .id(1L)
                .accountNumber("1111111111111111")
                .balance(new BigDecimal("5000"))
                .build();

        destinationAccount = Account.builder()
                .id(2L)
                .accountNumber("2222222222222222")
                .balance(new BigDecimal("1000"))
                .build();
    }

    @Test
    @DisplayName("Transfer should decrease source and increase destination balance")
    void process_validTransfer_updatesBalances() {
        BigDecimal transferAmount = new BigDecimal("2000");

        transferStrategy.process(transferAmount, sourceAccount, destinationAccount);

        assertEquals(new BigDecimal("3000"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("3000"), destinationAccount.getBalance());
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    @DisplayName("Transfer with null source account should throw exception")
    void process_nullSourceAccount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> transferStrategy.process(new BigDecimal("500"), null, destinationAccount));
    }

    @Test
    @DisplayName("Transfer with null destination account should throw exception")
    void process_nullDestinationAccount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> transferStrategy.process(new BigDecimal("500"), sourceAccount, null));
    }

    @Test
    @DisplayName("Transfer to same account should throw exception")
    void process_sameAccount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> transferStrategy.process(new BigDecimal("500"), sourceAccount, sourceAccount));
    }

    @Test
    @DisplayName("Transfer with insufficient balance should throw exception")
    void process_insufficientBalance_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> transferStrategy.process(new BigDecimal("10000"), sourceAccount, destinationAccount));
    }

    @Test
    @DisplayName("Transfer with zero amount should throw exception")
    void process_zeroAmount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> transferStrategy.process(BigDecimal.ZERO, sourceAccount, destinationAccount));
    }

    @Test
    @DisplayName("Transfer with negative amount should throw exception")
    void process_negativeAmount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> transferStrategy.process(new BigDecimal("-100"), sourceAccount, destinationAccount));
    }
}
