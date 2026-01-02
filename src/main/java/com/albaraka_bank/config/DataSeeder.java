package com.albaraka_bank.config;

import com.albaraka_bank.modules.account.model.Account;
import com.albaraka_bank.modules.account.repository.AccountRepository;
import com.albaraka_bank.modules.iam.model.User;
import com.albaraka_bank.modules.iam.model.UserRole;
import com.albaraka_bank.modules.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedUser("admin@albaraka.com", "Admin User", UserRole.ADMIN);
        seedUser("agent@albaraka.com", "Agent User", UserRole.AGENT_BANCAIRE);
        seedUser("client@albaraka.com", "Client User", UserRole.CLIENT);
    }

    private void seedUser(String email, String fullName, UserRole role) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = new User();
            user.setEmail(email);
            user.setFullName(fullName);
            user.setPassword(passwordEncoder.encode("secret"));
            user.setRole(role);
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());

            User savedUser = userRepository.save(user);
            System.out.println("Seeded user: " + email + " (password: secret)");

            if (role == UserRole.CLIENT) {
                seedAccount(savedUser);
            }
        }
    }

    private void seedAccount(User user) {
        if (accountRepository.findByOwner(user).isEmpty()) {
            Account account = new Account();
            account.setOwner(user);
            account.setAccountNumber("ALB" + (1000000000 + user.getId()));
            account.setBalance(new BigDecimal("0.00"));
            accountRepository.save(account);
            System.out.println("Seeded account for: " + user.getEmail());
        }
    }
}
