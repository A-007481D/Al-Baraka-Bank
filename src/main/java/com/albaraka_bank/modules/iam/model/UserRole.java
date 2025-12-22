package com.albaraka_bank.modules.iam.model;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    CLIENT,
    AGENT_BANCAIRE,
    ADMIN;

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }

    public java.util.Collection<? extends GrantedAuthority> getAuthorities() {
        return java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority(getAuthority()));
    }
}

