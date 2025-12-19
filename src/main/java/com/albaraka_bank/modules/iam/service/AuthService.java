package com.albaraka_bank.modules.iam.service;

import com.albaraka_bank.modules.account.service.AccountService;
import com.albaraka_bank.modules.iam.model.User;
import com.albaraka_bank.modules.iam.model.UserRole;
import com.albaraka_bank.modules.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;
        private final AccountService accountService;

    @Transactional
    public String register(String fullName, String email, String password, UserRole role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        var user = User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .active(true)
                .build();

        userRepository.save(user);

        if (role == UserRole.CLIENT) {
             accountService.createAccount(user);
        }

        return jwtService.generateToken(new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getRole().getAuthorities()));
    }

        public String login(String email, String password) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(email, password));
                var user = userRepository.findByEmail(email).orElseThrow();
                return jwtService.generateToken(new org.springframework.security.core.userdetails.User(
                                user.getEmail(),
                                user.getPassword(),
                                Collections.singletonList(
                                                new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))));
        }
}
