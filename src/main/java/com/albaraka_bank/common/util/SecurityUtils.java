package com.albaraka_bank.common.util;

import com.albaraka_bank.modules.iam.model.User;
import com.albaraka_bank.modules.iam.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;

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
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
