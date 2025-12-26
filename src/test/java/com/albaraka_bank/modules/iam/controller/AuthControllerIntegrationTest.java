package com.albaraka_bank.modules.iam.controller;

import com.albaraka_bank.modules.iam.dto.LoginRequest;
import com.albaraka_bank.modules.iam.dto.RegisterRequest;
import com.albaraka_bank.modules.iam.model.User;
import com.albaraka_bank.modules.iam.model.UserRole;
import com.albaraka_bank.modules.iam.repository.UserRepository;
import com.albaraka_bank.modules.account.repository.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Registration should create user and account, return JWT")
    void register_validRequest_createsUserAndReturnsToken() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));

        assertTrue(userRepository.findByEmail("test@example.com").isPresent());
        User user = userRepository.findByEmail("test@example.com").get();
        assertEquals(UserRole.CLIENT, user.getRole());
        assertTrue(accountRepository.findByOwner(user).isPresent());
    }

    @Test
    @DisplayName("Registration with duplicate email should fail")
    void register_duplicateEmail_returnsBadRequest() throws Exception {
        User existingUser = User.builder()
                .fullName("Existing User")
                .email("existing@example.com")
                .password(passwordEncoder.encode("password"))
                .role(UserRole.CLIENT)
                .active(true)
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        request.setFullName("New User");
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login with valid credentials should return JWT")
    void login_validCredentials_returnsToken() throws Exception {
        User user = User.builder()
                .fullName("Login User")
                .email("login@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.CLIENT)
                .active(true)
                .build();
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("login@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    @DisplayName("Login with invalid credentials should fail")
    void login_invalidCredentials_returnsUnauthorized() throws Exception {
        User user = User.builder()
                .fullName("User")
                .email("user@example.com")
                .password(passwordEncoder.encode("correctpassword"))
                .role(UserRole.CLIENT)
                .active(true)
                .build();
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }
}
