package com.albaraka_bank.modules.iam.controller;

import com.albaraka_bank.modules.iam.dto.AuthResponse;
import com.albaraka_bank.modules.iam.dto.LoginRequest;
import com.albaraka_bank.modules.iam.dto.RegisterRequest;
import com.albaraka_bank.modules.iam.model.UserRole;
import com.albaraka_bank.modules.iam.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        String token = authService.register(
                request.getFullName(),
                request.getEmail(),
                request.getPassword(),
                UserRole.CLIENT);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
