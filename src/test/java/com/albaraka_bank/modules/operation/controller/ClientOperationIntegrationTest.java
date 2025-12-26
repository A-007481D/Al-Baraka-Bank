package com.albaraka_bank.modules.operation.controller;

import com.albaraka_bank.modules.account.model.Account;
import com.albaraka_bank.modules.account.repository.AccountRepository;
import com.albaraka_bank.modules.iam.model.User;
import com.albaraka_bank.modules.iam.model.UserRole;
import com.albaraka_bank.modules.iam.repository.UserRepository;
import com.albaraka_bank.modules.iam.service.JwtService;
import com.albaraka_bank.modules.operation.dto.OperationRequest;
import com.albaraka_bank.modules.operation.model.OperationType;
import com.albaraka_bank.modules.operation.repository.OperationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ClientOperationIntegrationTest {

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

    private User clientUser;
    private Account clientAccount;
    private String clientToken;

    @BeforeEach
    void setUp() {
        operationRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

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
                .balance(new BigDecimal("50000"))
                .owner(clientUser)
                .build();
        accountRepository.save(clientAccount);

        clientToken = jwtService.generateToken(new org.springframework.security.core.userdetails.User(
                clientUser.getEmail(),
                clientUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT"))));
    }

    @Test
    @DisplayName("Deposit <= 10000 should be auto-approved (EXECUTED)")
    void createOperation_depositBelowThreshold_isExecuted() throws Exception {
        OperationRequest request = new OperationRequest();
        request.setType(OperationType.DEPOSIT);
        request.setAmount(new BigDecimal("5000"));

        mockMvc.perform(post("/api/client/operations")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXECUTED"));
    }

    @Test
    @DisplayName("Deposit > 10000 should require approval (PENDING)")
    void createOperation_depositAboveThreshold_isPending() throws Exception {
        OperationRequest request = new OperationRequest();
        request.setType(OperationType.DEPOSIT);
        request.setAmount(new BigDecimal("15000"));

        mockMvc.perform(post("/api/client/operations")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Withdrawal <= 10000 with sufficient balance should be executed")
    void createOperation_withdrawalBelowThreshold_isExecuted() throws Exception {
        OperationRequest request = new OperationRequest();
        request.setType(OperationType.WITHDRAWAL);
        request.setAmount(new BigDecimal("5000"));

        mockMvc.perform(post("/api/client/operations")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXECUTED"));
    }

    @Test
    @DisplayName("Transfer <= 10000 should be auto-approved")
    void createOperation_transferBelowThreshold_isExecuted() throws Exception {
        User destinationUser = User.builder()
                .fullName("Destination User")
                .email("destination@example.com")
                .password(passwordEncoder.encode("password"))
                .role(UserRole.CLIENT)
                .active(true)
                .build();
        userRepository.save(destinationUser);

        Account destinationAccount = Account.builder()
                .accountNumber("2222222222222222")
                .balance(new BigDecimal("1000"))
                .owner(destinationUser)
                .build();
        accountRepository.save(destinationAccount);

        OperationRequest request = new OperationRequest();
        request.setType(OperationType.TRANSFER);
        request.setAmount(new BigDecimal("5000"));
        request.setDestinationAccountNumber("2222222222222222");

        mockMvc.perform(post("/api/client/operations")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXECUTED"));
    }

    @Test
    @DisplayName("Get operations should return list of user operations")
    void getOperations_returnsUserOperations() throws Exception {
        mockMvc.perform(get("/api/client/operations")
                .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Unauthenticated request should be rejected")
    void createOperation_noAuth_returnsUnauthorized() throws Exception {
        OperationRequest request = new OperationRequest();
        request.setType(OperationType.DEPOSIT);
        request.setAmount(new BigDecimal("1000"));

        mockMvc.perform(post("/api/client/operations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }
}
