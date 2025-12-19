package com.albaraka_bank.modules.iam.controller;

import com.albaraka_bank.modules.account.service.AccountService;
import com.albaraka_bank.modules.iam.dto.UserRequest;
import com.albaraka_bank.modules.iam.dto.UserResponse;
import com.albaraka_bank.modules.iam.model.User;
import com.albaraka_bank.modules.iam.model.UserRole;
import com.albaraka_bank.modules.iam.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .active(request.getActive())
                .build();
        userRepository.save(user);

        if (request.getRole() == UserRole.CLIENT) {
            accountService.createAccount(user);
        }

        return ResponseEntity.ok(mapToResponse(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setRole(request.getRole());
        user.setActive(request.getActive());

        userRepository.save(user);
        return ResponseEntity.ok(mapToResponse(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> responses = users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private UserResponse mapToResponse(User user) {
        String accountNumber = null;
        if (user.getRole() == UserRole.CLIENT) {
            try {
                var account = accountService.getAccountByOwner(user);
                accountNumber = account.getAccountNumber();
            } catch (Exception e) {
            }
        }

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .accountNumber(accountNumber)
                .build();
    }
}
