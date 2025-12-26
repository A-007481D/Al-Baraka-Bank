package com.albaraka_bank.modules.operation.controller;

import com.albaraka_bank.modules.account.model.Account;
import com.albaraka_bank.modules.account.repository.AccountRepository;
import com.albaraka_bank.modules.iam.model.User;
import com.albaraka_bank.modules.iam.model.UserRole;
import com.albaraka_bank.modules.iam.repository.UserRepository;
import com.albaraka_bank.modules.iam.service.JwtService;
import com.albaraka_bank.modules.operation.model.Operation;
import com.albaraka_bank.modules.operation.model.OperationStatus;
import com.albaraka_bank.modules.operation.model.OperationType;
import com.albaraka_bank.modules.operation.repository.OperationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AgentOperationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User agentUser;
    private User clientUser;
    private Account clientAccount;
    private String agentToken;
    private Operation pendingOperation;

    @BeforeEach
    void setUp() {
        operationRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        agentUser = User.builder()
                .fullName("Agent User")
                .email("agent@example.com")
                .password(passwordEncoder.encode("password"))
                .role(UserRole.AGENT_BANCAIRE)
                .active(true)
                .build();
        userRepository.save(agentUser);

        clientUser = User.builder()
                .fullName("Client User")
                .email("client@example.com")
                .password(passwordEncoder.encode("password"))
                .role(UserRole.CLIENT)
                .active(true)
                .build();
        userRepository.save(clientUser);

        clientAccount = Account.builder()
                .accountNumber("1111111111111111")
                .balance(new BigDecimal("10000"))
                .owner(clientUser)
                .build();
        accountRepository.save(clientAccount);

        pendingOperation = Operation.builder()
                .type(OperationType.DEPOSIT)
                .amount(new BigDecimal("15000"))
                .status(OperationStatus.PENDING)
                .accountSource(clientAccount)
                .build();
        operationRepository.save(pendingOperation);

        agentToken = jwtService.generateToken(new org.springframework.security.core.userdetails.User(
                agentUser.getEmail(),
                agentUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_AGENT_BANCAIRE"))));
    }

    @Test
    @DisplayName("Get pending operations should return PENDING operations")
    void getPendingOperations_returnsPendingList() throws Exception {
        mockMvc.perform(get("/api/agent/operations/pending")
                .header("Authorization", "Bearer " + agentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("Approve operation should set status to EXECUTED and update balance")
    void approveOperation_updatesStatusAndBalance() throws Exception {
        BigDecimal initialBalance = clientAccount.getBalance();

        mockMvc.perform(put("/api/agent/operations/" + pendingOperation.getId() + "/approve")
                .header("Authorization", "Bearer " + agentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXECUTED"));

        Account updatedAccount = accountRepository.findById(clientAccount.getId()).orElseThrow();
        assertEquals(initialBalance.add(pendingOperation.getAmount()), updatedAccount.getBalance());
    }

    @Test
    @DisplayName("Reject operation should set status to CANCELLED without balance change")
    void rejectOperation_updatesStatusWithoutBalanceChange() throws Exception {
        BigDecimal initialBalance = clientAccount.getBalance();

        mockMvc.perform(put("/api/agent/operations/" + pendingOperation.getId() + "/reject")
                .header("Authorization", "Bearer " + agentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        Account updatedAccount = accountRepository.findById(clientAccount.getId()).orElseThrow();
        assertEquals(initialBalance, updatedAccount.getBalance());
    }

    @Test
    @DisplayName("CLIENT should not access agent endpoints")
    void agentEndpoint_accessedByClient_returnsForbidden() throws Exception {
        String clientToken = jwtService.generateToken(new org.springframework.security.core.userdetails.User(
                clientUser.getEmail(),
                clientUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT"))));

        mockMvc.perform(get("/api/agent/operations/pending")
                .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }
}
