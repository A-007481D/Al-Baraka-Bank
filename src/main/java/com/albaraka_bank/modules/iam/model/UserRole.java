package com.albaraka_bank.modules.iam.model;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    CLIENT,
    AGENT,
    ADMIN;

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }
}

