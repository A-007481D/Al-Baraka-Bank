package com.albaraka_bank.modules.iam.service;

import com.albaraka_bank.modules.account.service.AccountService;
import com.albaraka_bank.modules.iam.model.User;
import com.albaraka_bank.modules.iam.model.UserRole;
import com.albaraka_bank.modules.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KeycloakUserSyncService {

    private final UserRepository userRepository;
    private final AccountService accountService;

    @Transactional
    public User syncUser(Jwt jwt) {
        String email = extractEmail(jwt);
        String fullName = extractFullName(jwt);
        UserRole role = extractRole(jwt);

        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User newUser = User.builder()
                .email(email)
                .fullName(fullName)
                .password("")
                .role(role)
                .active(true)
                .build();

        userRepository.save(newUser);

        if (role == UserRole.CLIENT) {
            accountService.createAccount(newUser);
        }

        return newUser;
    }

    private String extractEmail(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email != null) {
            return email;
        }
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null && preferredUsername.contains("@")) {
            return preferredUsername;
        }
        throw new RuntimeException("No email found in Keycloak token");
    }

    private String extractFullName(Jwt jwt) {
        String name = jwt.getClaimAsString("name");
        if (name != null) {
            return name;
        }
        String givenName = jwt.getClaimAsString("given_name");
        String familyName = jwt.getClaimAsString("family_name");
        if (givenName != null || familyName != null) {
            return ((givenName != null ? givenName : "") + " " + (familyName != null ? familyName : "")).trim();
        }
        return jwt.getClaimAsString("preferred_username");
    }

    @SuppressWarnings("unchecked")
    private UserRole extractRole(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles.contains("admin")) {
                return UserRole.ADMIN;
            }
            if (roles.contains("agent")) {
                return UserRole.AGENT_BANCAIRE;
            }
        }
        return UserRole.CLIENT;
    }
}
