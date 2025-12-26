package com.albaraka_bank.common.util;

import com.albaraka_bank.modules.iam.model.User;
import com.albaraka_bank.modules.iam.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private static UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        SecurityUtils.userRepository = userRepository;
    }

    public static User getCurrentUser() {
        if (userRepository == null) {
            throw new IllegalStateException("UserRepository not initialized");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = extractEmail(auth);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private static String extractEmail(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("No authentication found");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof Jwt jwt) {
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

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            return userDetails.getUsername();
        }

        return auth.getName();
    }

    public static boolean isKeycloakAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof Jwt;
    }
}
